---
sidebar_position: 4
id: custom-type
title: "Custom Type"
---

# `Refined` Type - Custom Type

## What is a Custom `Refined` Type?

A custom `Refined` type is useful when your domain rule is not covered by a pre-defined type.

Use it when you want:
- a domain-specific name (e.g. `Month`, `OrderId`, `Username`)
- reusable validation logic in one place
- compile-time + runtime validation with one definition

## Import

```scala mdoc:reset-object
import refined4s.*
```

## Define a `Refined` Type

```scala
type RefinedTypeName = RefinedTypeName.Type
object RefinedTypeName extends Refined[ActualType] {
  override inline def invalidReason(a: ActualType): String =
    expectedMessage("something with blah blah")

  override inline def predicate(a: ActualType): Boolean =
    // validation logic here
}
```

Example:

```scala mdoc
type MyString = MyString.Type
object MyString extends Refined[String] {
  override inline def invalidReason(a: String): String =
    expectedMessage("a non-empty String")

  override inline def predicate(a: String): Boolean =
    a != ""
}
```

## Create Values

Given the following `Refined` type:

```scala mdoc
type Month = Month.Type
object Month extends Refined[Int] {
  override inline def invalidReason(a: Int): String =
    expectedMessage("Int between 1 and 12 (1 - 12)")

  override inline def predicate(a: Int): Boolean =
    a >= 1 && a <= 12
}
```

### Compile-time Validation (`apply`)

Use `apply` when the input is known at compile-time.

Valid cases:

```scala mdoc
Month(1)
Month(12)
```

Invalid cases:

```scala
Month(0)
// Month(0)
// ^^^^^^^^
// Invalid value: [0]. It must be Int between 1 and 12 (1 - 12)

Month(13)
// Month(13)
// ^^^^^^^^^
// Invalid value: [13]. It must be Int between 1 and 12 (1 - 12)
```

### Runtime Validation (`from`)

Use `from` when the input comes from runtime sources (request, DB, config, etc.).

```scala mdoc
val monthInput1 = 1
val monthInput2 = 12

val validMonthResult1: Either[String, Month] = Month.from(monthInput1)
val validMonthResult2: Either[String, Month] = Month.from(monthInput2)

validMonthResult1
validMonthResult2
```

```scala mdoc
val invalidMonthInput1 = 0
val invalidMonthInput2 = 13

Month.from(invalidMonthInput1)
Month.from(invalidMonthInput2)
```

### Functional Runtime Handling

```scala mdoc
def renderMonth(input: Int): String =
  Month.from(input).fold(
    error => s"Invalid month input: $error",
    month => s"Validated month: ${month.value}"
  )

renderMonth(5)
renderMonth(13)
```

### Runtime Unsafe Validation (`unsafeFrom`)

:::danger
`unsafeFrom` may throw an exception if the input value is invalid. Prefer `from` in most cases.
:::

```scala mdoc:crash
Month.unsafeFrom(invalidMonthInput2)
```

## Get Actual Value

Use `.value` to get the underlying value.

```scala mdoc
val month = Month(1)
month.value
```

## Pattern Matching

`Refined` provides `unapply`, so you can pattern match directly.

```scala mdoc
month match {
  case Month(value) =>
    println(s"Pattern matched value: $value")
}
```

## Guidelines

1. Keep `predicate` pure and deterministic.
2. Keep `invalidReason` specific and user-facing.
3. Prefer `from` at runtime boundaries for total, functional flows.
4. Use `apply` for literal constants where compile-time checking is possible.
