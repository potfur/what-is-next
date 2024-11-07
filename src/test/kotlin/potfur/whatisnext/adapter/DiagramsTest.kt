package potfur.whatisnext.adapter

import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.events.EventFilters.AddServiceName
import org.http4k.events.EventFilters.AddZipkinTraces
import org.http4k.events.HttpEvent.Incoming
import org.http4k.events.HttpEvent.Outgoing
import org.http4k.events.then
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters.ReportHttpTransaction
import org.http4k.filter.ServerFilters
import org.http4k.lens.Path
import org.http4k.lens.value
import org.http4k.tracing.Actor
import org.http4k.tracing.ActorType.System
import org.http4k.tracing.TraceRenderPersistence
import org.http4k.tracing.junit.TracerBulletEvents
import org.http4k.tracing.persistence.FileSystem
import org.http4k.tracing.renderer.MermaidSequenceDiagram
import org.http4k.tracing.tracer.HttpTracer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import potfur.whatisnext.ChunkAggregateWhatIsNext
import potfur.whatisnext.Error
import potfur.whatisnext.Fields
import potfur.whatisnext.FieldsChunk
import potfur.whatisnext.Id
import potfur.whatisnext.Option
import potfur.whatisnext.OptionsChunk
import potfur.whatisnext.ReadOnlyChunk
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.State.COMPLETED
import potfur.whatisnext.Specification.State.REQUIRED
import potfur.whatisnext.Specification.Type
import potfur.whatisnext.Storage
import potfur.whatisnext.branch
import potfur.whatisnext.thenOnValue
import java.io.File

class DiagramsTest : AdapterTestCase() {
    @RegisterExtension
    val events = TracerBulletEvents(
        listOf(HttpTracer { Actor(it.metadata["service"].toString(), System) }),
        listOf(MermaidSequenceDiagram),
        TraceRenderPersistence.FileSystem(File("src/test/resources"))
    )

    private val options = OptionsChunk(Storage(), listOf(Option.A, Option.B))
    private val fields = FieldsChunk(Storage())
    private val info = ReadOnlyChunk { _, _ -> Success("DONE") }

    private val flow = ChunkAggregateWhatIsNext(
        options.thenOnValue(
            branch(Option.A, info),
            branch(Option.B, fields.thenOnValue(branch("Dude", info)) { it?.firstName }),
        ) { it }
    )

    private val specLens = TestingJson.autoBody<DataEnvelope.Structured<List<Specification<out Type>>>>().toLens()
    private val statusLens = TestingJson.autoBody<DataEnvelope.Value<Boolean>>().toLens()
    private val genericLens = TestingJson.autoBody<DataEnvelope.Structured<List<GenericSpec>>>().toLens()
    private val validationLens = TestingJson.autoBody<DataEnvelope.Structured<List<Error>>>().toLens()

    private val optionsPayloadLens = TestingJson.autoBody<DataEnvelope.Value<Option?>>().toLens()
    private val fieldsPayloadLens = TestingJson.autoBody<DataEnvelope.Structured<Fields?>>().toLens()
    private val infoPayloadLens = TestingJson.autoBody<DataEnvelope.Value<String?>>().toLens()

    private val whatIsNextAdapter = WhatIsNextAdapter(
        basePath = basePath,
        flow = flow,
        specInjector = { r, v -> r.with(specLens of DataEnvelope.Structured(v)) },
        statusInjector = { r, v -> r.with(statusLens of DataEnvelope.Value(v)) },
        requesterResolver = requesterResolver
    )
    private val optionsAdapter = options.asHttpAdapter(
        basePath = basePath,
        requesterResolver = requesterResolver,
        dataInjector = { r, v -> r.with(optionsPayloadLens of DataEnvelope.Value(v)) },
        errorsInjector = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
        dataExtractor = { r -> optionsPayloadLens(r).data.value }
    )
    private val fieldsAdapter = fields.asHttpAdapter(
        basePath = basePath,
        requesterResolver = requesterResolver,
        dataInjector = { r, v -> r.with(fieldsPayloadLens of DataEnvelope.Structured(v)) },
        errorsInjector = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
        dataExtractor = { r -> fieldsPayloadLens(r).data }
    )
    private val infoAdapter = info.asHttpAdapter(
        basePath = basePath,
        requesterResolver = requesterResolver,
        dataInjector = { r, v -> r.with(infoPayloadLens of DataEnvelope.Value(v)) },
    )

    private val clientTracing = ClientFilters.RequestTracing()
        .then(ReportHttpTransaction {
            AddZipkinTraces().then(AddServiceName("Client")).then(events)(Outgoing(it))
        })
    private val serverTracing = ServerFilters
        .RequestTracing()
        .then(ReportHttpTransaction {
            AddZipkinTraces().then(AddServiceName("Application")).then(events)(Incoming(it))
        })
    private val http = clientTracing
        .then(serverTracing)
        .then((whatIsNextAdapter + optionsAdapter + fieldsAdapter + infoAdapter).asRoutingHttpAdapter())

    @Test
    fun `sequence diagram with single flow`() {
        Assertions.assertEquals(
            listOf(options.type.name to REQUIRED),
            Request.next(flowId).map { it.type to it.state }
        )

        Request.submitOption(flowId, Option.A)

        Assertions.assertEquals(
            listOf(options.type.name to COMPLETED, info.type.name to COMPLETED),
            Request.next(flowId).map { it.type to it.state }
        )

        Request.submitOption(flowId, Option.B)

        Assertions.assertEquals(
            listOf(options.type.name to COMPLETED, fields.type.name to REQUIRED),
            Request.next(flowId).map { it.type to it.state }
        )

        Request.submitFields(flowId, Fields("Dude", "Lebowski", "lebowski@dude.com"))

        Assertions.assertEquals(
            listOf(options.type.name to COMPLETED, fields.type.name to COMPLETED, info.type.name to COMPLETED),
            Request.next(flowId).map { it.type to it.state }
        )
    }

    private fun Request.Companion.next(flowId: Id) =
        Request(GET, "foo/{flowId}/next")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(http)
            .handleOrThrow(genericLens)
            .data

    private fun Request.Companion.submitOption(flowId: Id, option: Option) =
        Request(POST, "foo/{flowId}/option")
            .with(
                Path.value(Id).of("flowId") of flowId,
                optionsPayloadLens of DataEnvelope.Value(option)
            )
            .use(http)
            .handleOrThrow(validationLens)
            .also { Assertions.assertEquals(0, it.data.size) }

    private fun Request.Companion.submitFields(flowId: Id, data: Fields?) =
        Request(POST, "foo/{flowId}/field")
            .with(
                Path.value(Id).of("flowId") of flowId,
                fieldsPayloadLens of DataEnvelope.Structured(data)
            )
            .use(http)
            .handleOrThrow(validationLens)
}
