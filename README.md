# MCPUpdater

In the efforts to preserve old versions of Minecraft Forge, we've been taking steps to update the toolchain.  
One of these steps includes backporting the newer MCPConfig data format, so that these old versions have a form of data that is representable by the newer systems.

This is step one to compatibility with the newer ForgeGradle systems.

## Transformations

The purpose of MCPUpdater is to grab and transform the antiquated MCP data formats, with the following inclusions:

### Patches
The MCP patch format is as such:

* minecraft_ff/net.minecraft.advancements.Advancement.java.patch
* minecraft_merged_ff/net.minecraft.advancements.Advancement.java.patch
* minecraft_server_ff/net.minecraft.advancements.Advancement.java.patch

The MCP patches start with the header:

```
diff -r -U 3 minecraft\net\minecraft\advancements\AdvancementList.java minecraft_patched\net\minecraft\advancements\AdvancementList.java
--- minecraft\net\minecraft\advancements\AdvancementList.java
+++ minecraft_patched\net\minecraft\advancements\AdvancementList.java

diff -r -U 3 minecraft\net\minecraft\advancements\AdvancementList.java minecraft_patched\net\minecraft\advancements\AdvancementList.java
--- minecraft\net\minecraft\advancements\AdvancementList.java
+++ minecraft_patched\net\minecraft\advancements\AdvancementList.java

diff -r -U 3 minecraft_server\net\minecraft\advancements\AdvancementList.java minecraft_server_patched\net\minecraft\advancements\AdvancementList.java
--- minecraft_server\net\minecraft\advancements\AdvancementList.java
+++ minecraft_server_patched\net\minecraft\advancements\AdvancementList.java
```

The MCPConfig patch format is as such:

* patches/client/net/minecraft/advancements/Advancement.java.patch
* patches/joined/net/minecraft/advancements/Advancement.java.patch
* patches/server/net/minecraft/advancements/Advancement.java.patch

The MCPConfig patches start with the header:

```
--- a/net/minecraft/advancements/AdvancementList.java
+++ b/net/minecraft/advancements/AdvancementList.java
```

### EXC

The MCP data uses a file called joined.exc to encode data about constructor parameter naming, function access and exceptions thrown.

The EXC format is as such:
```
FQN.SRG(SIGNATURE)=EXCEPTIONS|PARAMETER NAMES[-ACCESS=<ACCESS>]

eg.
net/minecraft/client/Minecraft.func_71384_a()V=org/lwjgl/LWJGLException,java/io/IOException|
net/minecraft/client/Minecraft$2.<init>(Lnet/minecraft/client/Minecraft;)V=|p_i47419_1_
net/minecraft/server/dedicated/DedicatedServer.func_71190_q()V-Access=PUBLIC
```

The MCPConfig data uses three files - constructors.txt, exceptions.txt and access.txt to encode this information.

#### constructors.txt

Constructors.txt lines are in the format:

```
SRG_ID FQN SIGNATURE
```

Where the SRG_ID is the number preceding the second `_` character in the EXC Line:
For example, the example line 
```net/minecraft/client/Minecraft$2.<init>(Lnet/minecraft/client/Minecraft;)V=|p_i47419_1_```
will have the constructors.txt line

```
47419 net/minecraft/client/Minecraft$2 <init>(Lnet/minecraft/client/Minecraft;)V
```

#### exceptions.txt

Exceptions.txt lines are in the format:
```
CLASS/FUNCTION (PARAMS)RETURN ( EXCEPTION)+
```

For example, the example line
```
net/minecraft/client/Minecraft.func_71384_a()V=org/lwjgl/LWJGLException,java/io/IOException|
```
will have the exceptions.txt line

```
net/minecraft/client/Minecraft func_71384_a ()V org/lwjgl/LWJGLException java/io/IOException
```

#### access.txt

Access.txt lines are in the format:
```
ACCESS CLASS OBJECT SIGNATURE
```

For example, the example line 
```
net/minecraft/server/dedicated/DedicatedServer.func_71190_q()V-Access=PUBLIC
```
will have the access.txt line

```
PUBLIC net/minecraft/server/dedicated/DedicatedServer func_71190 ()V
```

### TSRG

The MCP data contains a file called "joined.srg". The MCPConfig data expects a file called "joined.tsrg".  
These formats were designed for intercompatibility, and thus they can be converted easily.

The existing method involves a python script based on [this file](https://github.com/MinecraftForge/MCPConfig/blob/master/update/SRGSorter.py).

```
dump_tsrg(srg_to_tsrg(file_in), file_out)
```

This script must be updated to use Java.

### Inject

The MCP data contains a directory structure similar to:
```
    │   Start.java
    │
    ├───inject
    │   │   package-info-template.java
    │   │
    │   └───common
    │       └───mcp
    │               MethodsReturnNonnullByDefault.java
```

MCPConfig expects this tree in the form:
```
├───inject
│   │   package-info-template.java
│   │
│   └───mcp
│       │   MethodsReturnNonnullByDefault.java
│       │
│       └───client
│               Start.java
```

## config.json

The configuration file is where all of these steps are accumulated.  
It defines the steps that ForgeGradle should take to apply our configuration, and the order.
For example, the 1.12.2 config.json:
```json
{
    "mcinjector": {
        "version": "de.oceanlabs.mcp:mcinjector:3.7.3:fatjar",
        "args": ["--in", "{input}", "--out", "{output}", "--log", "{log}", "--lvt=LVT", "--exc", "{exceptions}", "--acc", "{access}", "--ctr", "{constructors}"]
    },
    "fernflower": {
        "version": "net.minecraftforge:forgeflower:1.0.342.8",
        "args": ["-din=1", "-rbr=1", "-dgs=1", "-asc=1", "-rsy=1", "-iec=1", "-jvn=1", "-log=TRACE", "-cfg", "{libraries}", "{input}", "{output}"],
        "jvmargs": ["-Xmx4G"]
    },
    "merge": {
        "version": "net.minecraftforge:mergetool:0.2.3.2:fatjar",
        "args": ["--client", "{client}", "--server", "{server}", "--ann", "{version}", "--output", "{output}"],
        "jvmargs": []
    },
    "rename": {
        "version": "net.md-5:SpecialSource:1.8.3:shaded",
        "args": ["--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}", "--kill-source"],
        "repo": "https://repo1.maven.org/maven2/"
    },
    "libraries": {
        "client": ["com.google.code.findbugs:jsr305:3.0.1"],
        "server": ["com.google.code.findbugs:jsr305:3.0.1"],
        "joined": ["com.google.code.findbugs:jsr305:3.0.1", "net.minecraftforge:mergetool:0.2.3.2:forge"]
    }
}
```

It defines the steps `mcinjector`, `fernflower`, `merge` and `rename` along with the programs that should be retrieved and how to run them.

Libraries is unimportant for now.

