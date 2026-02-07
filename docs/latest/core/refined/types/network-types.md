---
sidebar_position: 4
id: refined-network-types
title: "Network Types"
---
# Refined Network Types

## Import

```scala mdoc
import refined4s.types.all.*
```

## Refined `Uri`

`Uri` is a refined `String` type that accepts only valid URI values.

### Compile-time Validation
```scala mdoc
Uri("https://www.google.com")
Uri("https://github.com/kevin-lee")
```
```scala
Uri("%^<>[]`{}")
// error:
// Invalid value: ["%^<>[]`{}"]. It must be a URI String
```

### Runtime Validation
```scala mdoc
val validUri1 = "https://www.google.com" 
Uri.from(validUri1)

val validUri2 = "https://github.com/kevin-lee" 
Uri.from(validUri2)
```
```scala mdoc
val invalidUri = "%^<>[]`{}" 
Uri.from(invalidUri)
```

### Get Value

```scala mdoc
val uriA = Uri("https://www.google.com")
val uriB = Uri("https://github.com/kevin-lee")

uriA.value

uriB.value
```

### Convert to `java.net.URI`

```scala mdoc
uriA.toURI

uriB.toURI
```


## Refined `Url`

`Url` is a refined `String` type that accepts only valid URL values with supported protocols.

### Compile-time Validation
```scala mdoc
Url("https://www.google.com")
Url("https://github.com/kevin-lee")
```
```scala
Url("blah://test.com")

// Url("blah://test.com")
// ^^^^^^^^^^^^^^^^^^^^^^
// Invalid Url value: [blah://test.com]. unknown protocol: blah
```

### Runtime Validation
```scala mdoc
val validUrl1 = "https://www.google.com" 
Url.from(validUrl1)

val validUrl2 = "https://github.com/kevin-lee" 
Url.from(validUrl2)
```
```scala mdoc
val invalidUrl = "%^<>[]`{}" 
Url.from(invalidUrl)
```

### Get Value

```scala mdoc
val urlA = Url("https://www.google.com")
val urlB = Url("https://github.com/kevin-lee")

urlA.value

urlB.value
```

### Convert to `java.net.URI`

```scala mdoc
urlA.toURI

urlB.toURI
```


## Refined `PortNumber`

`PortNumber` is a refined `Int` type for valid TCP/UDP port numbers in the range `0` to `65535`.

### Compile-time Validation
```scala mdoc
PortNumber(0)

PortNumber(65535)
```
```scala
PortNumber(-1)
// PortNumber(-1)
// ^^^^^^^^^^^^^^
// Invalid value: [-1]. It has to be Int between 0 and 65535 (0 <= PortNumber <= 65535)


PortNumber(65536)
// PortNumber(65536)
// ^^^^^^^^^^^^^^^^^
// Invalid value: [65536]. It has to be Int between 0 and 65535 (0 <= PortNumber <= 65535)
```

### Runtime Validation
```scala mdoc
val validPortNumber1 = 0
PortNumber.from(validPortNumber1)

val validPortNumber2 = 65535
PortNumber.from(validPortNumber2)
```
```scala mdoc
val invalidPortNumber1 = -1
PortNumber.from(invalidPortNumber1)

val invalidPortNumber2 = 65536
PortNumber.from(invalidPortNumber2)
```

### Get Value

```scala mdoc
val portNumberA = PortNumber(80)
val portNumberB = PortNumber(9000)

portNumberA.value

portNumberB.value
```


## Refined `SystemPortNumber`

`SystemPortNumber` is a refined `Int` type for system ports in the range `0` to `1023`.

### Compile-time Validation
```scala mdoc
SystemPortNumber(0)

SystemPortNumber(1023)
```
```scala
SystemPortNumber(-1)
// SystemPortNumber(-1)
// ^^^^^^^^^^^^^^^^^^^^
// Invalid value: [-1]. It has to be Int between 0 and 1023 (0 <= SystemPortNumber <= 1023)


SystemPortNumber(1024)
// SystemPortNumber(1024)
// ^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [1024]. It has to be Int between 0 and 1023 (0 <= SystemPortNumber <= 1023)
```

### Runtime Validation
```scala mdoc
val validSystemPortNumber1 = 0
SystemPortNumber.from(validSystemPortNumber1)

val validSystemPortNumber2 = 1023
SystemPortNumber.from(validSystemPortNumber2)
```
```scala mdoc
val invalidSystemPortNumber1 = -1
SystemPortNumber.from(invalidSystemPortNumber1)

val invalidSystemPortNumber2 = 1024
SystemPortNumber.from(invalidSystemPortNumber2)
```

### Get Value

```scala mdoc
val systemPortNumberA = SystemPortNumber(22)
val systemPortNumberB = SystemPortNumber(80)

systemPortNumberA.value

systemPortNumberB.value
```


## Refined `NonSystemPortNumber`

`NonSystemPortNumber` is a refined `Int` type for non-system ports in the range `1024` to `65535`.

### Compile-time Validation
```scala mdoc
NonSystemPortNumber(1024)

NonSystemPortNumber(65535)
```
```scala
NonSystemPortNumber(1023)
// NonSystemPortNumber(1023)
// ^^^^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [1023]. It has to be Int between 1024 and 65535 (1024 <= NonSystemPortNumber <= 65535)


NonSystemPortNumber(65536)
// NonSystemPortNumber(65536)
// ^^^^^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [65536]. It has to be Int between 1024 and 65535 (1024 <= NonSystemPortNumber <= 65535)
```

### Runtime Validation
```scala mdoc
val validNonSystemPortNumber1 = 1024
NonSystemPortNumber.from(validNonSystemPortNumber1)

val validNonSystemPortNumber2 = 65535
NonSystemPortNumber.from(validNonSystemPortNumber2)
```
```scala mdoc
val invalidNonSystemPortNumber1 = 1023
NonSystemPortNumber.from(invalidNonSystemPortNumber1)

val invalidNonSystemPortNumber2 = 65536
NonSystemPortNumber.from(invalidNonSystemPortNumber2)
```

### Get Value

```scala mdoc
val nonSystemPortNumberA = NonSystemPortNumber(8080)
val nonSystemPortNumberB = NonSystemPortNumber(54321)

nonSystemPortNumberA.value

nonSystemPortNumberB.value
```


## Refined `UserPortNumber`

`UserPortNumber` is a refined `Int` type for user (registered) ports in the range `1024` to `49151`.

### Compile-time Validation
```scala mdoc
UserPortNumber(1024)

UserPortNumber(49151)
```
```scala
UserPortNumber(1023)
// UserPortNumber(1023)
// ^^^^^^^^^^^^^^^^^^^^
// Invalid value: [1023]. It has to be Int between 1024 and 49151 (1024 <= UserPortNumber <= 49151)


UserPortNumber(49152)
// UserPortNumber(49152)
// ^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [49152]. It has to be Int between 1024 and 49151 (1024 <= UserPortNumber <= 49151)
```

### Runtime Validation
```scala mdoc
val validUserPortNumber1 = 1024
UserPortNumber.from(validUserPortNumber1)

val validUserPortNumber2 = 49151
UserPortNumber.from(validUserPortNumber2)
```
```scala mdoc
val invalidUserPortNumber1 = 1023
UserPortNumber.from(invalidUserPortNumber1)

val invalidUserPortNumber2 = 49152
UserPortNumber.from(invalidUserPortNumber2)
```

### Get Value

```scala mdoc
val userPortNumberA = UserPortNumber(8888)
val userPortNumberB = UserPortNumber(33333)

userPortNumberA.value

userPortNumberB.value
```


## Refined `DynamicPortNumber`

`DynamicPortNumber` is a refined `Int` type for dynamic/private ports in the range `49152` to `65535`.

### Compile-time Validation
```scala mdoc
DynamicPortNumber(49152)

DynamicPortNumber(65535)
```
```scala
DynamicPortNumber(49151)
// DynamicPortNumber(49151)
// ^^^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [49151]. It has to be Int between 49152 and 65535 (49152 <= DynamicPortNumber <= 65535)


DynamicPortNumber(65536)
// DynamicPortNumber(65536)
// ^^^^^^^^^^^^^^^^^^^^^^^^
// Invalid value: [65536]. It has to be Int between 49152 and 65535 (49152 <= DynamicPortNumber <= 65535)
```

### Runtime Validation
```scala mdoc
val validDynamicPortNumber1 = 49152
DynamicPortNumber.from(validDynamicPortNumber1)

val validDynamicPortNumber2 = 65535
DynamicPortNumber.from(validDynamicPortNumber2)
```
```scala mdoc
val invalidDynamicPortNumber1 = 49151
DynamicPortNumber.from(invalidDynamicPortNumber1)

val invalidDynamicPortNumber2 = 65536
DynamicPortNumber.from(invalidDynamicPortNumber2)
```

### Get Value

```scala mdoc
val dynamicPortNumberA = DynamicPortNumber(55555)
val dynamicPortNumberB = DynamicPortNumber(65432)

dynamicPortNumberA.value

dynamicPortNumberB.value
```
