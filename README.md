[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/manojkumar-kasiviswanathan/Javabasic">
    <img src="logo.png" alt="Logo" width="100" height="100">
  </a>
</div>

<h3 align="center">Java Basic</h3>

A simple Java project that covers all the basic Java concepts

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#conditional-assignment">Conditional Assignment</a>
    </li>
    <li>
      <a href="#Loops">Loops</a>
      <ul>
        <li><a href="#foreach">foreach</a></li>
        <li><a href="#for">for</a></li>
        <li><a href="#do">do</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

## Conditional Assignment

This operator consists of three operands and is used to evaluate Boolean expressions. The goal of the operator is to
decide; which value should be assigned to the variable. The operator is written as:

#### usage:

```java
public class ConditionalAssignment {
    int a = 5, b = 10;
    int greaterNumber = a > b ? a : b;
}
```

## Loops

The Java for loop is used to iterate a part of the program several times. If the number of iteration is fixed, it is
recommended to use for loop.

### foreach

#### usage:

```java
public class Looping {
    public static void forEach() {
        int values[] = {1, 2, 3, 4, 5};
        for (int value : values) {
            System.out.println(value);
        }
    }
}
```

### for

#### usage:

```java
public class Looping {
    public static void forloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        for (int i = 0; alphabets.length > i; i++) {
            System.out.println(alphabets[i]);
        }
    }
}
```

### while

#### usage:

```java
public class Looping {
    public static void whileloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        System.out.println("while loop");
        int i = 0;
        while (alphabets.length > i) {
            System.out.println(alphabets[i]);
            i++;
        }
    }
}
```

### do

#### usage:

```java
public class Looping {
    public static void doloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        System.out.println("do loop");
        int i = 0;
        do {
            System.out.println(alphabets[i]);
            i++;
        } while (alphabets.length > i);

    }
}
```

[contributors-shield]: https://img.shields.io/badge/Contributors-1-%3CCOLOR%3E?style=for-the-badge

[contributors-url]: https://github.com/manojkumar-kasiviswanathan/Javabasic/graphs/contributors

[issues-shield]: https://img.shields.io/github/issues/manojkumar-kasiviswanathan/JavaBasic?color=yellow&style=for-the-badge

[issues-url]: https://github.com/manojkumar-kasiviswanathan/Javabasic/issues

[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge

[license-url]: https://github.com/manojkumar-kasiviswanathan/Javabasic/blob/main/LICENSE.txt

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555

[linkedin-url]: https://www.linkedin.com/in/manojkumar-kasiviswanathan-7a8aa973/

[forks-shield]: https://img.shields.io/github/forks/manojkumar-kasiviswanathan/Javabasic?style=for-the-badge

[forks-url]: https://github.com/manojkumar-kasiviswanathan/Javabasic/network/members

[stars-shield]: https://img.shields.io/github/stars/manojkumar-kasiviswanathan/Javabasic.svg?style=for-the-badge

[stars-url]: https://github.com/manojkumar-kasiviswanathan/Best-README-Template/stargazers