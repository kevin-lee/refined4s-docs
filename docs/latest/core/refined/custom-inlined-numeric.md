---
sidebar_position: 3
id: custom-inlined-numeric
title: "Custom Inlined Numeric"
---

## `InlinedNumericMinMax`

`InlinedNumericMinMax` is a helper trait for numeric custom refined types that have
both minimum and maximum bounds.

It gives you:
- inclusive range validation (`minValue <= value <= maxValue`)
- built-in `predicate` and `invalidReason`
- inline validation support for literals with `apply` (compile-time), plus runtime validation with `from`

Use it when your type is a numeric value constrained to a closed interval.

### Import

```scala mdoc
import refined4s.types.all.*
```
Or
```scala
import refined4s.types.numeric.*
```

### How to use

1. Define your custom type as `type X = X.Type`.
2. `X` extends `InlinedNumericMinMax[A]` from `refined4s.types.numeric` or `refined4s.types.all`.
3. Implement `minValue` and `maxValue` as `inline def`.
4. Use `apply` for literals and `from` for runtime values.

#### Example

```scala mdoc
type Percent = Percent.Type
object Percent extends InlinedNumericMinMax[Int] {
  override inline def minValue: Int = 0
  override inline def maxValue: Int = 100
}
```

:::info NOTE
the `minValue` and `maxValue` have to be defined as `inline def` so that they can be used at compile-time.
:::


### Create with Compile-time Validation

#### Compile-time Validation (`apply`)
```scala mdoc
Percent(0)
Percent(100)
```

```scala
Percent(-1)
// Percent(-1)
// ^^^^^^^^^^^
// Invalid value: [-1]. It must be >= 0 && <= 100.

Percent(101)
// Percent(101)
// ^^^^^^^^^^^^
// Invalid value: [101]. It must be >= 0 && <= 100.
```

### Create with Runtime Validation

#### Runtime Validation (`from`)
```scala mdoc
val percentInput1 = 20
val percentInput2 = 120

Percent.from(percentInput1)
Percent.from(percentInput2)
```

#### Runtime Unsafe Validation (`unsafeFrom`)

:::danger
`unsafeFrom` may throw an exception if the input value is invalid. So it is not recommended to use it.
:::

```scala mdoc:crash
Percent.unsafeFrom(percentInput2)
```
