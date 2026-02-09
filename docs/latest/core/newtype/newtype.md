---
sidebar_position: 1
id: newtype
title: "Newtype"
---

## What is `Newtype`?

`Newtype` lets you create domain-specific types from existing value types with zero runtime overhead.

`Newtype` gives you:
- stronger type-safety at compile-time
- the same runtime representation as the original type
- clear domain boundaries without wrappers or allocations

`Newtype` does **not** perform validation.
If you need validation rules, use `Refined` or `InlinedRefined`.

## Import

```scala mdoc:reset-object
import refined4s.*
```

## Define a `Newtype`

```scala
type NewtypeName = NewtypeName.Type
object NewtypeName extends Newtype[ActualType]
```

Example:

```scala mdoc
type Name = Name.Type
object Name extends Newtype[String]
```

## Create Values

```scala
val newtypeName = NewtypeName(value)
```

```scala mdoc
val name = Name("Kevin")
```

## Get Actual Value

Use `.value` to unwrap the underlying value.

```scala
newtypeName.value
```

```scala mdoc
name.value
```

## Pattern Matching

`Newtype` provides `unapply`, so you can pattern match directly.

```scala mdoc
name match {
  case Name(value) =>
    println(s"Pattern matched value: $value")
}
```

## Type-Safety Example

```scala mdoc:reset-object
import refined4s.*

type Name = Name.Type
object Name extends Newtype[String]

type Email = Email.Type
object Email extends Newtype[String]

def hello(name: Name): Unit = println(s"Hello ${name.value}")

def send(email: Email): Unit = println(s"Sending email to ${email.value}")

val name = Name("Kevin")
hello(name)

val email = Email("kevin@blah.blah")
send(email)
```

```scala mdoc:fail
hello("Kevin")
```

```scala mdoc:fail
send("kevin@blah.blah")
```

## Functional Typeclass Derivation

`Newtype` can derive typeclass instances from its underlying type using `deriving`.

e.g.) Sorting the `Name` from the example above fails because `Name` does not have an `Ordering` instance.
```scala mdoc:fail
List(Name("c"), Name("a"), Name("b")).sorted
```

You can easily derive an `Ordering` instance for `Name` using `deriving`. So `Ordering[Name]` is derived from `Ordering[String]`.

```scala mdoc
given Ordering[Name] = Name.deriving[Ordering]

List(Name("c"), Name("a"), Name("b")).sorted
```
