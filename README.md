cglib [![Build Status](https://travis-ci.org/cglib/cglib.svg?branch=master)](https://travis-ci.org/cglib/cglib)
================

***IMPORTANT NOTE: cglib is unmaintained and does not work well (or possibly at all?) in newer JDKs, particularly JDK17+. If you need to support newer JDKs, we will accept well-tested well-thought-out patches... but you'll probably have better luck migrating to something like [ByteBuddy](https://bytebuddy.net).***

Byte Code Generation Library is high level API to generate and transform JAVA byte code.
It is used by AOP, testing, data access frameworks to generate dynamic proxy objects and intercept field access.
https://github.com/cglib/cglib/wiki

How To: https://github.com/cglib/cglib/wiki/How-To

Latest Release: https://github.com/cglib/cglib/releases/latest

All Releases: https://github.com/cglib/cglib/releases

cglib-#.#_#.jar             binary distribution, CGLIB classes only, 
it must be used to extend cglib classes dependant on ASM API 

cglib-nodep-#.#_#.jar       binary distribution, CGLIB and renamed ASM classes, 
not extendable 
