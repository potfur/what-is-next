# What's Next

The _What's Next_ is a - let's call it - a pattern, intended for moving control over data collection from client apps,
to backend. This allows clients to focus on presentation of independent sections of such forms without knowing 
what is the order of sections, what are the branches, validation rules etc.

## DataChunk

All the aforementioned sections are represented as data chunks. For example an address form can be a data chunk,
a drop-down would be another, a single checkbox for terms and conditions agreement is also one.
Some sections can be only providing block of text, e.g. additional information or explanation, 
would be a read-only data chunk.
Such sections can be linked with other sections, for example - when in parent section specific value is selected, 
a new subsections will appear.

Each of data chunks, is composed of two elements: specification and interaction interface.

The specification, informs client application what are the requirements of that data chunk, is it required, optional or completed, 
and no interaction from user is needed. 
It can contain list of fields (and their requirements) for which data is collected, a list of options to choose from and so on.

The interaction interface tells what can be done with chunk, and it can be defined as two types: immutable and mutable.
The immutable data chunk, presents just data to the client application, for example a cost breakdown based on already provided data.
The mutable data chunk provides additionally methods to validate, submit or clear/remove data.

The data chunk definition is [here](./src/main/kotlin/potfur/whatisnext/DataChunk.kt), and some sample implementations:
- [FieldsChunk](./src/testFixtures/kotlin/potfur/whatisnext/FieldsChunk.kt) - a _form_ with 3 fields,
- [OptionsChunk](./src/testFixtures/kotlin/potfur/whatisnext/OptionsChunk.kt) - a _dropdown_ with list of possible choices,
- [ReadOnlyChunk](./src/testFixtures/kotlin/potfur/whatisnext/ReadOnlyChunk.kt) - a _read-only_ chunk, for presenting additional info

All the data chunks, should be implemented as completely independent elements.
it will increase their reusability (ie. same data chunk for sender and recipient) and allows for easier composition.

## WhatIsNext

The [WhatIsNext](./src/main/kotlin/potfur/whatisnext/WhatIsNext.kt) interface is the place where data collection flow, all the connections between chunks are defined and presented to client applications.
It contains two methods:
- `whatIsNext` - that will provide specifications of all chunks that can be interacted with at that stage of the data collection flow,
- `isCompleted` - will tell if the data collection is completed or there's something to do.

Default implementation comes with [ChunkAggregateWhatIsNext](./src/main/kotlin/potfur/whatisnext/WhatIsNext.kt#L27), that brings its own small language of methods for building flows.

- [chunk.on(predicate)](./src/test/kotlin/potfur/whatisnext/WhatIsNextChunkOnConditionTest.kt) - `chunk` visibility depends on `predicate`
- [chunk.then(other)](./src/test/kotlin/potfur/whatisnext/WhatIsNextSingleParentThenSingleChildTest.kt) - allows making chunk or chunks appear when parent or parents are completed:
  - [single parent with single child](./src/test/kotlin/potfur/whatisnext/WhatIsNextSingleParentThenSingleChildTest.kt)
  - [single parent with multiple children](./src/test/kotlin/potfur/whatisnext/WhatIsNextSingleParentThenMultipleChildrenTest.kt)
  - [multiple parents with single child](./src/test/kotlin/potfur/whatisnext/WhatIsNextMultipleParentsThenSingleChildTest.kt)
  - [multiple parents with multiple children](./src/test/kotlin/potfur/whatisnext/WhatIsNextMultipleParentsThenMultipleChildrenTest.kt)
- [chunk.thenOnValue()](./src/test/kotlin/potfur/whatisnext/WhatIsNextBranchingTest.kt) - allows for _branching_ depending on the value from `chunk`, since the value access is passed via function, it can easily handle dropdowns/options, forms and read-only chunks.
