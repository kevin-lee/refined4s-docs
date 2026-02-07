---
sidebar_position: 3
id: refined-string-types
title: "String Types"
---
# Refined String Types

## Import

```scala mdoc
import refined4s.types.all.*
```

## Refined `NonEmptyString`

### Compile-time Validation
```scala mdoc
NonEmptyString("blah")
NonEmptyString("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")
```
```scala
NonEmptyString("")
// error:
// Invalid value: [""]. It must be a non-empty String
```

### Runtime Validation
```scala mdoc
val validNonEmptyString1 = "blah" 
NonEmptyString.from(validNonEmptyString1)

val validNonEmptyString2 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry." 
NonEmptyString.from(validNonEmptyString2)
```
```scala mdoc
val invalidNonEmptyString = "" 
NonEmptyString.from(invalidNonEmptyString)
```

### Concatenation

```scala mdoc
val nonEmptyString1 = NonEmptyString("Hello")
val nonEmptyString2 = NonEmptyString(" World")

nonEmptyString1 ++ nonEmptyString2
```

### Get Value

```scala mdoc
val nonEmptyStringA = NonEmptyString("blah")
val nonEmptyStringB = NonEmptyString("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")

nonEmptyStringA.value

nonEmptyStringB.value
```


***

## Refined `NonBlankString`

### Compile-time Validation
```scala mdoc
NonBlankString("blah")
NonBlankString("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")
```
```scala
// error:
// NonBlankString("")
// ^^^^^^^^^^^^^^^^^^
// Invalid value: [""]. It must be not all whitespace non-empty String

NonBlankString(" ")
// error:
// NonBlankString(" ")
// ^^^^^^^^^^^^^^^^^^^
// Invalid value: [" "]. It must be not all whitespace non-empty String

NonBlankString(" \t \n \r")
// error:
// NonBlankString(" \t \n \r")
// ^^^^^^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [" \t \n \r"]. It must be not all whitespace non-empty String
```

### Runtime Validation
```scala mdoc
val validNonBlankString1 = "blah"
NonBlankString.from(validNonBlankString1)

val validNonBlankString2 = "Lorem Ipsum is simply dummy text of the printing and typesetting industry."
NonBlankString.from(validNonBlankString2)
```
```scala mdoc
val invalidNonBlankString1 = ""
NonBlankString.from(invalidNonBlankString1)

val invalidNonBlankString2 = " "
NonBlankString.from(invalidNonBlankString2)

val invalidNonBlankString3 = " \t \n \r"
NonBlankString.from(invalidNonBlankString3)
```

### Concatenation

```scala mdoc
val nonBlankString1 = NonBlankString("Hello")
val nonBlankString2 = NonBlankString(" World")

nonBlankString1 ++ nonBlankString2

nonBlankString1.appendString("      ")

nonBlankString1.prependString("      ")
```

### Get Value

```scala mdoc
val nonBlankStringA = NonBlankString("blah")
val nonBlankStringB = NonBlankString("Lorem Ipsum is simply dummy text of the printing and typesetting industry.")

nonBlankStringA.value

nonBlankStringB.value
```


***

## Refined `Uuid`

### Compile-time Validation
```scala mdoc:reset-object
import refined4s.types.all.*
Uuid("3596f062-a6bd-4d2c-978e-3ed6f97a264b")

val uuid1 = java.util.UUID.randomUUID()
Uuid(uuid1)
```

```scala
Uuid("")
// error:
// Invalid value: [""]. It must be UUID

Uuid("blah")
// error:
// Invalid value: ["blah"]. It must be UUID
```

### Runtime Validation
```scala mdoc
val validUuidString = "3596f062-a6bd-4d2c-978e-3ed6f97a264b" 
Uuid.from(validUuidString)
```
```scala mdoc
val invalidUuid = "iuhsfd9f-f32wfwf3-d1i2j" 
Uuid.from(invalidUuid)
```

### To `java.util.UUID`

```scala mdoc
val uuid2 = Uuid("3596f062-a6bd-4d2c-978e-3ed6f97a264b")

uuid2.toUUID
```

### Get Value

```scala mdoc
val uuid3 = Uuid("3596f062-a6bd-4d2c-978e-3ed6f97a264b")

uuid3.value
```
