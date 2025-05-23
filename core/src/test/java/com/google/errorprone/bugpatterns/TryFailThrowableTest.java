/*
 * Copyright 2013 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author adamwos@google.com (Adam Wos)
 */
@RunWith(JUnit4.class)
public class TryFailThrowableTest {

  private final CompilationTestHelper compilationHelper =
      CompilationTestHelper.newInstance(TryFailThrowable.class, getClass());

  @Test
  public void positiveCases() {
    compilationHelper
        .addSourceLines(
            "TryFailThrowablePositiveCases.java",
"""
package com.google.errorprone.bugpatterns.testdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * @author adamwos@google.com (Adam Wos)
 */
public class TryFailThrowablePositiveCases {

  public static void emptyCatch_failNoMessage() {
    try {
      dummyMethod();
      Assert.fail();
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
    }
  }

  public static void commentCatch_failNoMessage() {
    try {
      dummyMethod();
      Assert.fail();
      // BUG: Diagnostic contains: catch (Exception t123)
    } catch (Throwable t123) {
      // expected!
      ;
      /* that's an empty comment */
    }
  }

  public static void commentCatch_failWithMessage() {
    try {
      dummyMethod();
      Assert.fail("Faaail!");
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  public static void commentCatch_failNotLast() {
    try {
      dummyMethod();
      fail("Faaail!");
      dummyMethod();
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  public static void commentCatch_assert() {
    try {
      dummyMethod();
      assertEquals(1, 2);
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  public static void commentCatch_assertNotLast() {
    try {
      dummyMethod();
      assertTrue("foobar!", true);
      dummyRecover();
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  public static void customMoreAsserts() {
    try {
      dummyMethod();
      CustomMoreAsserts.assertFoobar();
      dummyMethod();
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  public static void customMoreAsserts_fail() {
    try {
      dummyMethod();
      CustomMoreAsserts.fail("param", 0x42);
      dummyMethod();
      // BUG: Diagnostic contains: catch (Exception t)
    } catch (Throwable t) {
      // expected!
    }
  }

  static final class SomeTest extends TestCase {
    public void testInTestCase() {
      try {
        dummyMethod();
        fail("message");
        // BUG: Diagnostic contains: catch (Exception codeCatch_oldAssertFailWithMessage)
      } catch (Throwable codeCatch_oldAssertFailWithMessage) {
        // comment
        /* another */
      }
    }
  }

  static final class CustomMoreAsserts {
    static void assertFoobar() {}

    static void fail(String param1, int param2) {}
  }

  private static void dummyRecover() {}

  private static void dummyMethod() {}

  public static void catchesAssertionError() {
    try {
      dummyMethod();
      Assert.fail();
      // BUG: Diagnostic contains: remove this line
    } catch (AssertionError e) {
    }
  }

  public static void hasMessage() {
    try {
      dummyMethod();
      Assert.fail("foo");
      // BUG: Diagnostic contains: remove this line
    } catch (AssertionError e) {
    }
  }

  public static void catchesError_lastStatement() {
    try {
      dummyMethod();
      Assert.fail();
      // BUG: Diagnostic contains: remove this line
    } catch (Error e) {
    }
  }

  public static void catchesError_notLastStatement() {
    try {
      dummyMethod();
      Assert.fail();
      // BUG: Diagnostic contains: boolean threw = false;
    } catch (Error e) {
    }

    assertTrue(true);
  }

  public static void catchesError_nested() {
    for (Object o : Arrays.asList()) {
      try {
        dummyMethod();
        Assert.fail();
        // BUG: Diagnostic contains: boolean threw = false;
      } catch (Error e) {
      }
    }
  }

  public static void catchesError_nestedNoBlock() {
    for (Object o : Arrays.asList())
      try {
        dummyMethod();
        Assert.fail();
        // BUG: Diagnostic contains: boolean threw = false;
      } catch (Error e) {
      }
  }
}\
""")
        .doTest();
  }

  @Test
  public void negativeCases() {
    compilationHelper
        .addSourceLines(
            "TryFailThrowableNegativeCases.java",
            """
            package com.google.errorprone.bugpatterns.testdata;

            import static org.junit.Assert.assertEquals;
            import static org.junit.Assert.assertTrue;
            import static org.junit.Assert.fail;

            import junit.framework.TestCase;
            import org.junit.Assert;

            /**
             * @author adamwos@google.com (Adam Wos)
             */
            public class TryFailThrowableNegativeCases {

              public static void withoutFail() {
                try {
                  dummyMethod();
                } catch (Throwable t) {
                  dummyRecover();
                }
              }

              public static void failOutsideTry() {
                try {
                  dummyMethod();
                } catch (Throwable t) {
                  dummyRecover();
                }
                Assert.fail();
              }

              public static void withoutCatch() {
                try {
                  dummyMethod();
                  Assert.fail("");
                } finally {
                  dummyRecover();
                }
              }

              /** For now, this isn't supported. */
              public static void multipleCatches() {
                try {
                  dummyMethod();
                  Assert.fail("1234");
                } catch (Error e) {
                  dummyRecover();
                } catch (Throwable t) {
                  dummyRecover();
                }
              }

              public static void failNotLast() {
                try {
                  dummyMethod();
                  Assert.fail("Not last :(");
                  dummyMethod();
                } catch (Throwable t) {
                  dummyRecover();
                }
              }

              public static void catchException() {
                try {
                  dummyMethod();
                  Assert.fail();
                } catch (Exception t) {
                  dummyRecover();
                }
              }

              public static void catchException_failWithMessage() {
                try {
                  dummyMethod();
                  Assert.fail("message");
                } catch (Exception t) {
                  dummyRecover();
                }
              }

              public static void codeCatch_failNoMessage() {
                try {
                  dummyMethod();
                  Assert.fail();
                } catch (Throwable t) {
                  dummyRecover();
                }
              }

              public static void codeCatch_failWithMessage() {
                try {
                  dummyMethod();
                  Assert.fail("Faaail!");
                } catch (Throwable t444) {
                  dummyRecover();
                }
              }

              public static void codeCatch_staticImportedFail() {
                try {
                  dummyMethod();
                  fail();
                } catch (Throwable t444) {
                  dummyRecover();
                }
              }

              @SuppressWarnings("deprecation") // deprecated in JUnit 4.11
              public static void codeCatch_oldAssertFail() {
                try {
                  dummyMethod();
                  junit.framework.Assert.fail();
                } catch (Throwable codeCatch_oldAssertFail) {
                  dummyRecover();
                }
              }

              @SuppressWarnings("deprecation") // deprecated in JUnit 4.11
              public static void codeCatch_oldAssertFailWithMessage() {
                try {
                  dummyMethod();
                  junit.framework.Assert.fail("message");
                } catch (Throwable codeCatch_oldAssertFailWithMessage) {
                  dummyRecover();
                }
              }

              public static void codeCatch_FQFail() {
                try {
                  dummyMethod();
                  org.junit.Assert.fail("Faaail!");
                } catch (Throwable t444) {
                  dummyRecover();
                }
              }

              public static void codeCatch_assert() {
                try {
                  dummyMethod();
                  assertEquals(1, 2);
                } catch (Throwable t) {
                  dummyMethod();
                }
              }

              public static void commentCatch_assertNotLast() {
                try {
                  dummyMethod();
                  assertTrue("foobar!", true);
                  dummyRecover();
                } catch (Throwable t) {
                  dummyMethod();
                }
              }

              static final class SomeTest extends TestCase {
                public void testInTestCase() {
                  try {
                    dummyMethod();
                    fail("message");
                  } catch (Throwable codeCatch_oldAssertFailWithMessage) {
                    dummyRecover();
                  }
                }
              }

              private static void dummyRecover() {}

              private static void dummyMethod() {}
            }\
            """)
        .doTest();
  }
}
