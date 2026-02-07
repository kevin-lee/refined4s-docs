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

## `InlinedNumericMin`

`InlinedNumericMin` is a helper trait for numeric custom refined types that have
only a minimum bound.

It gives you:
- lower-bound validation (`minValue <= value`)
- built-in `predicate` and `invalidReason`
- inline validation support for literals with `apply` (compile-time), plus runtime validation with `from`

Use it when your type is a numeric value constrained to a lower-bound inclusive interval.

### How to use

1. Define your custom type as `type X = X.Type`.
2. `X` extends `InlinedNumericMin[A]` from `refined4s.types.numeric` or `refined4s.types.all`.
3. Implement `minValue` as `inline def`.
4. Use `apply` for literals and `from` for runtime values.

#### Example

```scala mdoc
type NonNegativeCount = NonNegativeCount.Type
object NonNegativeCount extends InlinedNumericMin[Int] {
  override inline def minValue: Int = 0
}
```

:::info NOTE
the `minValue` has to be defined as `inline def` so that it can be used at compile-time.
:::

### Create with Compile-time Validation

#### Compile-time Validation (`apply`)
```scala mdoc
NonNegativeCount(0)
NonNegativeCount(10)
```

```scala
NonNegativeCount(-1)
// NonNegativeCount(-1)
// ^^^^^^^^^^^^^^^^^^^^
// Invalid value: [-1]. It must be >= 0.
```

### Create with Runtime Validation

#### Runtime Validation (`from`)
```scala mdoc
val nonNegativeInput1 = 3
val nonNegativeInput2 = -3

NonNegativeCount.from(nonNegativeInput1)
NonNegativeCount.from(nonNegativeInput2)
```

#### Runtime Unsafe Validation (`unsafeFrom`)

:::danger
`unsafeFrom` may throw an exception if the input value is invalid. So it is not recommended to use it.
:::

```scala mdoc:crash
NonNegativeCount.unsafeFrom(nonNegativeInput2)
```

## `InlinedNumericMax`

`InlinedNumericMax` is a helper trait for numeric custom refined types that have
only a maximum bound.

It gives you:
- upper-bound validation (`value <= maxValue`)
- built-in `predicate` and `invalidReason`
- inline validation support for literals with `apply` (compile-time), plus runtime validation with `from`

Use it when your type is a numeric value constrained to an upper-bound inclusive interval.

### How to use

1. Define your custom type as `type X = X.Type`.
2. `X` extends `InlinedNumericMax[A]` from `refined4s.types.numeric` or `refined4s.types.all`.
3. Implement `maxValue` as `inline def`.
4. Use `apply` for literals and `from` for runtime values.

#### Example

```scala mdoc
type ScoreOutOf100 = ScoreOutOf100.Type
object ScoreOutOf100 extends InlinedNumericMax[Int] {
  override inline def maxValue: Int = 100
}
```

:::info NOTE
the `maxValue` has to be defined as `inline def` so that it can be used at compile-time.
:::

### Create with Compile-time Validation

#### Compile-time Validation (`apply`)
```scala mdoc
ScoreOutOf100(0)
ScoreOutOf100(100)
```

```scala
ScoreOutOf100(101)
// ScoreOutOf100(101)
// ^^^^^^^^^^^^^^^^^^
// Invalid value: [101]. It must be <= 100.
```

### Create with Runtime Validation

#### Runtime Validation (`from`)
```scala mdoc
val scoreInput1 = 80
val scoreInput2 = 120

ScoreOutOf100.from(scoreInput1)
ScoreOutOf100.from(scoreInput2)
```

#### Runtime Unsafe Validation (`unsafeFrom`)

:::danger
`unsafeFrom` may throw an exception if the input value is invalid. So it is not recommended to use it.
:::

```scala mdoc:crash
ScoreOutOf100.unsafeFrom(scoreInput2)
```
