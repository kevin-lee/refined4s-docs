---
sidebar_position: 3
id: inlined-custom-type
title: "Inlined Custom Type"
---

# `InlinedRefined` Type - Inlined Custom Type

## What is `InlinedRefined`?

`InlinedRefined` is `Newtype` + validation with explicit `inline` compile-time validation hooks.

It extends `RefinedBase`, so you still get runtime validation APIs such as:
- `from(a): Either[String, Type]`
- `unsafeFrom(a): Type`
- `value` / `unapply`

In addition, `InlinedRefined` provides `apply` with inline validation through:
- `inlinedPredicate`
- `inlinedExpectedValue`

## Import

```scala mdoc:reset-object
import refined4s.*
```

## Required Members

To define a custom `InlinedRefined[A]`, implement all of these:

1. `inlinedExpectedValue`: compile-time expected value message fragment.
2. `inlinedPredicate`: compile-time predicate for `apply`.
3. `invalidReason`: runtime error message for `from` and `unsafeFrom`.
4. `predicate`: runtime predicate for `from` and `unsafeFrom`.

:::info NOTE
Keep `inlinedPredicate` and `predicate` logically the same to avoid compile-time/runtime behavior mismatch.
:::

## Define an `InlinedRefined` Type

```scala mdoc
type NonEmptyName = NonEmptyName.Type
object NonEmptyName extends InlinedRefined[String] {

  override inline def inlinedExpectedValue: String =
    "a non-empty String"

  override inline def inlinedPredicate(inline a: String): Boolean =
    a != ""

  override def invalidReason(a: String): String =
    expectedMessage(inlinedExpectedValue)

  override def predicate(a: String): Boolean =
    a != ""
}
```

## Create Values

### Compile-time Validation (`apply`)

```scala mdoc
NonEmptyName("Kevin")
```

```scala
NonEmptyName("")
// NonEmptyName("")
// ^^^^^^^^^^^^^^^^^
// Invalid value: [""]. It must be a non-empty String.
```

### Runtime Validation (`from`)

```scala mdoc
val nameInput1 = "Kevin"
val nameInput2 = ""

NonEmptyName.from(nameInput1)
NonEmptyName.from(nameInput2)

```

### Functional Runtime Handling

```scala mdoc
def renderName(input: String): String =
  NonEmptyName.from(input).fold(
    error => s"Invalid name: $error",
    name => s"Valid name: ${name.value}"
  )

renderName("Kevin")
renderName("")
```

### Runtime Unsafe Validation (`unsafeFrom`)

:::danger
`unsafeFrom` may throw an exception if the input value is invalid. Prefer `from` in most cases.
:::

```scala mdoc:crash
NonEmptyName.unsafeFrom(nameInput2)
```

## Get Actual Value

```scala mdoc
val validName = NonEmptyName("Tom")
validName.value
```

## Pattern Matching

```scala mdoc
validName match {
  case NonEmptyName(value) =>
    println(s"Pattern matched name: $value")
}
```
