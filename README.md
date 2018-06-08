IdeaLingua Petstore
==============================

A sample distributed Pet Store application showing how to wire together clients and servers written in different languages via [IdeaLingua](https://github.com/pshirshov/izumi-r2) API definitions.

IdeaLingua
----------

[IdeaLingua](https://github.com/pshirshov/izumi-r2) is an RPC framework that lets you effortlessly wire together distributed applications composed of components written in any language.

You can find out more on [our website](https://izumi.7mind.io/idealingua/index.html).

Language Support Matrix
-----------------------

At the moment we support the following languages:

| Language / Platform | Server | Client  |
|-----------|----------------------------|----------------------------|
| **Scala / JVM**          | **Yes** [(example)](./servers/scala-jvm-server) | **Yes** [(example)](./clients/scala-jvm-client) |
| **TypeScript / Node.js** | TBD     | **Yes** [(example)](./clients/typescript-node-client) |
| **Go / Native**          | **Yes** | **Yes** |
| **C# / .NET**             | **Yes** | TBD |

Some of them already have sample Pet Store implementations. Others are yet to be written.

If you want your language to be supported by IdeaLingua, just submit a [Pull Request](https://github.com/pshirshov/izumi-r2)!
