---
sidebar_position: 1
id: refined
title: "Refined"
---

import DocCardList from '@theme/DocCardList';

## What is `Refined`?

`Refined` is `Newtype` + validation. It lets you model validated domain values as types instead of using raw primitives everywhere.

A refined value keeps:
- the original value type (`String`, `Int`, etc.)
- validation logic (`predicate`)
- a clear error message (`invalidReason`)

## Import

```scala mdoc:reset-object
import refined4s.*
```

## Why Use Refined Types?

- Avoid primitive obsession by modeling domain meaning in the type.
- Catch invalid literals at compile-time when possible.
- Keep runtime validation explicit and total with `Either`.
- Make boundaries safer without sacrificing plain Scala values.

## More About `Refined` Types:

<DocCardList />
