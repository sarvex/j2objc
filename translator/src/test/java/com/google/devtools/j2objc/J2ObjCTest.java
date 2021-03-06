/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc;

import com.google.devtools.j2objc.Options.OutputStyleOption;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link com.google.devtools.j2objc.J2ObjC#run(List)}.
 */
public class J2ObjCTest extends GenerationTest {
  String jarPath;
  String exampleJavaPath;
  String packageInfoPath;

  public void setUp() throws IOException {
    super.setUp();

    List<String> classpathEntries = Options.getClassPathEntries();
    for (String entry : getComGoogleDevtoolsJ2objcPath()) {
      classpathEntries.add(entry);
    }

    jarPath = getResourceAsFile("util/example.jar");
    exampleJavaPath = getResourceAsFile("util/Example.java");
    packageInfoPath = getResourceAsFile("util/package-info.java");
  }

  public void tearDown() throws Exception {
    Options.getClassPathEntries().clear();
    Options.setBatchTranslateMaximum(0);
    super.tearDown();
  }

  // No assertions for package_info_h--nothing interesting in it that other tests don't assert.
  private void makeAssertions(String example_h, String example_m, String package_info_m)
      throws Exception {
    // These assertions should hold for any generated files, including (in the future)
    // combined jar generation.
    assertTranslation(example_h, "Generated by the J2ObjC translator.");
    assertTranslation(example_h, "interface CBTExample : NSObject");
    assertTranslation(example_h, "- (instancetype)init;");
    assertTranslation(example_h, "J2OBJC_EMPTY_STATIC_INIT(CBTExample)");
    assertTranslation(example_h, "typedef CBTExample");
    assertTranslation(example_h, "J2OBJC_TYPE_LITERAL_HEADER(CBTExample)");
    assertTranslation(example_m, "@implementation CBTExample");
    assertTranslation(example_m, "- (instancetype)init {");
    assertTranslation(example_m, "+ (const J2ObjcClassInfo *)__metadata {");
    assertTranslation(example_m, "J2OBJC_CLASS_TYPE_LITERAL_SOURCE(CBTExample)");
    assertTranslation(package_info_m, "#include \"IOSClass.h\"");
    assertTranslation(
        package_info_m, "#include \"com/google/j2objc/annotations/ObjectiveCName.h\"");
    assertTranslation(package_info_m, "+ (IOSObjectArray *)__annotations {");

    assertErrorCount(0);
  }

  private void makeAssertionsForJar() throws Exception {
    String example_h = getTranslatedFile("com/google/test/Example.h");
    String example_m = getTranslatedFile("com/google/test/Example.m");
    String package_info_h = getTranslatedFile("com/google/test/package-info.h");
    String package_info_m = getTranslatedFile("com/google/test/package-info.m");
    // Test the file header comments.
    assertTranslation(example_h, "source: jar:file:");
    assertTranslation(example_m, "source: jar:file:");
    assertTranslation(package_info_h, "source: jar:file:");
    assertTranslation(package_info_m, "source: jar:file:");
    assertTranslation(example_h, jarPath);
    assertTranslation(example_m, jarPath);
    assertTranslation(package_info_h, jarPath);
    assertTranslation(package_info_m, jarPath);
    assertTranslation(example_h, "com/google/test/Example.java");
    assertTranslation(example_m, "com/google/test/Example.java");
    assertTranslation(package_info_h, "com/google/test/package-info.java");
    assertTranslation(package_info_m, "com/google/test/package-info.java");
    // Test the includes
    assertTranslation(example_h, "#ifndef _CBTExample_H_");
    assertTranslation(example_m, "#include \"com/google/test/Example.h\"");
    assertTranslation(package_info_h, "#ifndef _CBTpackage_info_H_");
    assertTranslation(package_info_m, "@interface ComGoogleTestpackage_info : NSObject");
    assertTranslation(package_info_m, "@implementation ComGoogleTestpackage_info");
    // All other assertions
    makeAssertions(example_h, example_m, package_info_m);
  }

  public void testCompilingFromJar() throws Exception {
    J2ObjC.run(Collections.singletonList(jarPath));
    makeAssertionsForJar();
  }

  public void testBatchCompilingFromJar() throws Exception {
    Options.setBatchTranslateMaximum(2);
    J2ObjC.run(Collections.singletonList(jarPath));
    makeAssertionsForJar();
  }

  // Make assertions for java files with default output locations.
  private void makeAssertionsForJavaFiles() throws Exception {
    String example_h = getTranslatedFile("com/google/devtools/j2objc/util/Example.h");
    String example_m = getTranslatedFile("com/google/devtools/j2objc/util/Example.m");
    String package_info_h = getTranslatedFile("com/google/devtools/j2objc/util/package-info.h");
    String package_info_m = getTranslatedFile("com/google/devtools/j2objc/util/package-info.m");
    assertTranslation(example_m, "#include \"com/google/devtools/j2objc/util/Example.h\"");
    makeAssertionsForJavaFiles(example_h, example_m, package_info_h, package_info_m);
  }

  private void makeAssertionsForJavaFiles(
      String example_h, String example_m, String package_info_h, String package_info_m)
      throws Exception {
    // Test the file header comments.
    assertTranslation(example_h, exampleJavaPath);
    assertTranslation(example_m, exampleJavaPath);
    assertTranslation(package_info_h, packageInfoPath);
    assertTranslation(package_info_m, packageInfoPath);
    // Test the includes
    assertTranslation(example_h, "#ifndef _CBTExample_H_");
    assertTranslation(
        package_info_m, "@interface ComGoogleDevtoolsJ2objcUtilpackage_info : NSObject");
    assertTranslation(package_info_h, "#ifndef _CBTpackage_info_H_");
    assertTranslation(package_info_m, "@implementation ComGoogleDevtoolsJ2objcUtilpackage_info");
    // All other assertions
    makeAssertions(example_h, example_m, package_info_m);
  }

  public void testCompilingFromFiles() throws Exception {
    J2ObjC.run(Arrays.asList(exampleJavaPath, packageInfoPath));
    makeAssertionsForJavaFiles();
  }

  public void testBatchCompilingFromFiles() throws Exception {
    Options.setBatchTranslateMaximum(2);
    J2ObjC.run(Arrays.asList(exampleJavaPath, packageInfoPath));
    makeAssertionsForJavaFiles();
  }

  private void makeAssertionsForCombinedJar() throws Exception {
    String combined_h = getTranslatedFile("example.h");
    String combined_m = getTranslatedFile("example.m");
    makeAssertions(combined_h, combined_m, combined_m);
  }

  public void testCombinedJar() throws Exception {
    Options.setOutputStyle(OutputStyleOption.SOURCE_COMBINED);
    J2ObjC.run(Collections.singletonList(jarPath));
    makeAssertionsForCombinedJar();
  }

  public void testSourceDirsOption() throws Exception {
    Options.setOutputStyle(Options.OutputStyleOption.SOURCE);
    J2ObjC.run(Arrays.asList(exampleJavaPath, packageInfoPath));
    String example_h = getTranslatedFile(exampleJavaPath.replace(".java", ".h"));
    String example_m = getTranslatedFile(exampleJavaPath.replace(".java", ".m"));
    String package_info_h = getTranslatedFile(packageInfoPath.replace(".java", ".h"));
    String package_info_m = getTranslatedFile(packageInfoPath.replace(".java", ".m"));
    makeAssertionsForJavaFiles(example_h, example_m, package_info_h, package_info_m);
  }

  // Test a simple annotation processor on the classpath.
  public void testAnnotationProcessing() throws Exception {
    String processorPath = getResourceAsFile("annotations/Processor.jar");
    Options.getClassPathEntries().add(processorPath);

    String examplePath = getResourceAsFile("annotations/Example.java");
    J2ObjC.run(Collections.singletonList(examplePath));
    assertErrorCount(0);


    String translatedAnnotationHeader = getTranslatedFile("ProcessingResult.h");
    String translatedAnnotationImpl = getTranslatedFile("ProcessingResult.m");

    // Our dummy annotation processor is very simple--it always creates a class with no package,
    // ProcessingResult, with a minimal implementation.
    assertTranslation(translatedAnnotationHeader, "@interface ProcessingResult : NSObject");
    assertTranslation(translatedAnnotationHeader, "- (NSString *)getResult;");
    assertTranslation(translatedAnnotationImpl, "@implementation ProcessingResult");
    assertTranslation(translatedAnnotationImpl, "return @\"ObjectiveCName\"");
  }
}
