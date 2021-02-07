package uk.gemwire.mcpconvert.convert;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JoinedExcSplitterTest {

    @Test
    void testConstructors() {
        testExcLine("com/example/test/TestConstructors.<init>(Ljava/lang/Class;)V=|p_i3141_1_",
            "3141 com/example/test/TestConstructors (Ljava/lang/Class;)V",
            null,
            null);

        testExcLine("com/example/test/TestConstructors.<init>(Ljava/util/function/BiFunction;)Z=|p_i11235_1_",
            "11235 com/example/test/TestConstructors (Ljava/util/function/BiFunction;)Z",
            null,
            null);
    }

    @Test
    void testExceptions() {
        testExcLine("com/example/test/TestExceptions.one(Ljava/lang/Object;)V=java/lang/Exception|",
            null,
            "com/example/test/TestExceptions one (Ljava/lang/Object;)V java/lang/Exception",
            null);

        testExcLine("com/example/test/TestExceptions.two()Ljava/util/function/Function;=java/lang/Exception,java/lang/Error|",
            null,
            "com/example/test/TestExceptions two ()Ljava/util/function/Function; java/lang/Exception java/lang/Error",
            null);

        testExcLine(
            "com/example/test/TestExceptions.three(Ljava/lang/Error;)Z=org/example/ExampleException," +
                "java/lang/RuntimeException,java/lang/Error|",
            null,
            "com/example/test/TestExceptions three (Ljava/lang/Error;)Z org/example/ExampleException " +
                "java/lang/RuntimeException java/lang/Error",
            null);
    }

    @Test
    void testAccess() {
        testExcLine("com/example/test/TestAccess.one(I)Z-Access=PUBLIC",
            null,
            null,
            "PUBLIC com/example/test/TestAccess one (I)Z");

        testExcLine("com/example/test/TestAccess.two(ZZZIZZ)V-Access=PRIVATE",
            null,
            null,
            "PRIVATE com/example/test/TestAccess two (ZZZIZZ)V");
    }

    @Test
    void testMixedCE() {
        testExcLine("com/example/test/TestMixed.<init>(Ljava/lang/ClassLoader;)V=java/lang/Exception|p_i1337_1_",
            "1337 com/example/test/TestMixed (Ljava/lang/ClassLoader;)V",
            "com/example/test/TestMixed <init> (Ljava/lang/ClassLoader;)V java/lang/Exception",
            null);
    }

    @Test
    void testMixedCA() {
        testExcLine("com/example/test/TestMixed.<init>(JLjava/io/InputStream;)V=|p_i756_0_,p_i756_2_-Access=PUBLIC",
            "756 com/example/test/TestMixed (JLjava/io/InputStream;)V",
            null,
            "PUBLIC com/example/test/TestMixed <init> (JLjava/io/InputStream;)V");
    }

    @Test
    void testMixedEA() {
        testExcLine("com/example/test/TestMixed.two(Ljava/util/Optional;)I=java/io/IOException|-Access=PROTECTED",
            null,
            "com/example/test/TestMixed two (Ljava/util/Optional;)I java/io/IOException",
            "PROTECTED com/example/test/TestMixed two (Ljava/util/Optional;)I");
    }

    @Test
    void testMixedAll() {
        testExcLine(
            "com/example/test/TestMixed.<init>(Jjava/lang/ErrorJZ)V=java/lang/IOException," +
                "org/example/OopsException|p_i124816_0_,p_i124816_2_p_i124816_3_p_i124816_5_-Access=PROTECTED",
            "124816 com/example/test/TestMixed (Jjava/lang/ErrorJZ)V",
            "com/example/test/TestMixed <init> (Jjava/lang/ErrorJZ)V java/lang/IOException org/example/OopsException",
            "PROTECTED com/example/test/TestMixed <init> (Jjava/lang/ErrorJZ)V");
    }

    void testExcLine(String line, @Nullable String constructor, @Nullable String exceptions, @Nullable String access) {
        Result res = testExcLine(line);
        Assertions.assertEquals(constructor, res.constructor);
        Assertions.assertEquals(exceptions, res.exceptions);
        Assertions.assertEquals(access, res.access);
    }

    Result testExcLine(String line) {
        String[] results = new String[3];
        JoinedExcSplitter.parseExcLine(line,
            constructor -> results[0] = constructor,
            exceptions -> results[1] = exceptions,
            access -> results[2] = access);
        return new Result(results[0], results[1], results[2]);
    }

    static record Result(@Nullable String constructor, @Nullable String exceptions, @Nullable String access) {}
}
