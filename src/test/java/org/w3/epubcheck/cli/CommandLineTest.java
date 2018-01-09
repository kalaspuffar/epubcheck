package org.w3.epubcheck.cli;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.test.NoExitSecurityManager;
import com.adobe.epubcheck.test.common;
import com.adobe.epubcheck.test.common.TestOutputType;
import com.adobe.epubcheck.tool.Checker;
import com.adobe.epubcheck.util.*;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3.epubcheck.common.CommonTestFunctions;

import java.io.*;
import java.net.URL;

public class CommandLineTest
{
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private SecurityManager originalManager;
  private PrintStream originalOut;
  private PrintStream originalErr;


  @Before
  public void setUp() throws Exception
  {
    this.originalManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void tearDown() throws Exception
  {
    System.setSecurityManager(this.originalManager);
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void StaticClassTest()
  {
    //This will create an instance of classes that have nothing but static methods for the sake of code coverage.
    Checker checker = new Checker();
    Assert.assertTrue("Checker string isn't as expected", checker.toString().startsWith("com.adobe.epubcheck.tool.Checker"));

    HandlerUtil handlerUtil = new HandlerUtil();
    Assert.assertTrue("HandlerUtil string isn't as expected", handlerUtil.toString().startsWith("com.adobe.epubcheck.util.HandlerUtil"));

    PathUtil pathUtil = new PathUtil();
    Assert.assertTrue("PathUtil string isn't as expected", pathUtil.toString().startsWith("com.adobe.epubcheck.util.PathUtil"));

    CheckUtil checkUtil = new CheckUtil();
    Assert.assertTrue("CheckUtil string isn't as expected", checkUtil.toString().startsWith("com.adobe.epubcheck.util.CheckUtil"));

    ResourceUtil resourceUtil = new ResourceUtil();
    Assert.assertTrue("ResourceUtil string isn't as expected", resourceUtil.toString().startsWith("com.adobe.epubcheck.util.ResourceUtil"));
  }

  @Test
  public void EmptyTest()
  {
    CommonTestFunctions.runCustomTest(CommandLineTest.class, "empty", 1);
    Assert.assertEquals("Command output not as expected", Messages.get("argument_needed"), errContent.toString().trim());
  }

  @Test
  public void HelpTest()
  {
    CommonTestFunctions.runCustomTest(CommandLineTest.class, "help", 1, true, "-?");
    Assert.assertEquals("Command output not as expected", Messages.get("no_file_specified"), errContent.toString().trim());
    String expected = String.format(Messages.get("help_text").replaceAll("[\\s]+", " "), EpubCheck.version());
    String actual = outContent.toString();
    actual = actual.replaceAll("[\\s]+", " ");
    Assert.assertTrue("Help output isn't as expected", actual.contains(expected));
  }

  @Test
  public void ConflictingOutputTest()
  {
    CommonTestFunctions.runCustomTest(CommandLineTest.class, "conflicting_output", 1, "-o", "foo.xml", "-j", "bar.json");
    Assert.assertEquals("Command output not as expected", Messages.get("output_type_conflict"), errContent.toString().trim());
  }

  @Test
  public void SeveritiesUsageTest()
  {
    runSeverityTest("severity", "severity_usage", 1, "-u");
  }

  @Test
  public void SeveritiesWarningTest()
  {
    runSeverityTest("severity", "severity_warning", 1, "-w");
  }

  @Test
  public void SeveritiesErrorTest()
  {
    runSeverityTest("severity", "severity_error", 1, "-e");
  }

  @Test
  public void SeveritiesFatalTest()
  {
    runSeverityTest("severity", "severity_fatal", 0, "-f");
  }

  @Test
  public void SeveritiesOverrideOkTest()
  {
    String testName = "severity_overrideOk";
    URL inputUrl = CommandLineTest.class.getResource("");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideMissingFileTest()
  {
    String testName = "severity_overrideMissingFile";
    URL inputUrl = CommandLineTest.class.getResource("");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadIdTest()
  {
    String testName = "severity_overrideBadId";
    URL inputUrl = CommandLineTest.class.getResource("");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadSeverityTest()
  {
    String testName = "severity_overrideBadSeverity";
    URL inputUrl = CommandLineTest.class.getResource("");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void SeveritiesOverrideBadMessageTest()
  {
    String testName = "severity_overrideBadMessage";
    URL inputUrl = CommandLineTest.class.getResource("");
    String inputPath = inputUrl.getPath();
    String configFile = inputPath + "/" + testName + ".txt";
    runSeverityTest("severity", testName, 1, "-c", configFile, "-u");
  }

  @Test
  public void PassonWarningsTest()
  {
    runExtraCommandLineArgTest("passonwarnings", 0, new String[0]);
  }

  @Test
  public void JsonOutputTest()
  {
    CommonTestFunctions.runExpTest(CommandLineTest.class, "jsonfile", 0, CommonTestFunctions.TestOutputType.JSON, true, new String[0]);
  }

  @Test
  public void XMLOutputTest()
  {
    CommonTestFunctions.runExpTest(CommandLineTest.class, "xmlfile", 0, CommonTestFunctions.TestOutputType.XML, true, new String[0]);
  }

  @Test
  public void XMPOutputTest()
  {
    CommonTestFunctions.runExpTest(CommandLineTest.class, "xmlfile", 0, CommonTestFunctions.TestOutputType.XMP, true, new String[0]);
  }

  @Test
  public void FailOnWarningsTest()
  {
    String[] extraArgs = {"--failonwarnings"};
    runExtraCommandLineArgTest("failonwarnings", 1, extraArgs);
  }


  private static void runExtraCommandLineArgTest(String testName, int expectedReturnCode, String[] extraArgs)
  {
    CommonTestFunctions.runExpTest(CommandLineTest.class, testName, expectedReturnCode, CommonTestFunctions.TestOutputType.JSON, false, extraArgs);
  }

  private static void runSeverityTest(String epubName, String testName, int expectedReturnCode, String... args)
  {
    File actualOutput;
    PrintStream ps = null;
    PrintStream origErr = System.err;
    PrintStream origOut = System.out;
    try
    {
      String[] theArgs = new String[3 + args.length];
      URL inputUrl = CommandLineTest.class.getResource(epubName);
      Assert.assertNotNull("Input folder is missing.", inputUrl);
      String inputPath = inputUrl.getPath();
      String outputPath = inputPath + "/../" + testName + "_actual_results.txt";

      theArgs[0] = inputPath;
      theArgs[1] = "-mode";
      theArgs[2] = "exp";
      System.arraycopy(args, 0, theArgs, 3, args.length);

      actualOutput = new File(outputPath);
      ps = new PrintStream(actualOutput);
      System.setErr(ps);
      System.setOut(ps);
      CommonTestFunctions.runCustomTest(CommandLineTest.class, testName, expectedReturnCode, theArgs);
      System.setErr(origErr);
      System.setOut(origOut);
      ps.flush();
      ps.close();
      ps = null;

      Assert.assertTrue("Output file is missing.", actualOutput.exists());
      URL expectedUrl = CommandLineTest.class.getResource(testName + "_expected_results.txt");
      Assert.assertNotNull("Expected file is missing.", expectedUrl);
      File expectedOutput = new File(expectedUrl.getPath());
      Assert.assertTrue("Expected file is missing.", expectedOutput.exists());
      try
      {
        CommonTestFunctions.compareText(expectedOutput, actualOutput);
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
      File tempFile = new File(testName + ".epub");

      Assert.assertFalse("Temp file left over after test: " + tempFile.getPath(), tempFile.exists());
    }
    catch (FileNotFoundException ex)
    {
      System.err.println("File not found: " + testName + "_actual_results.txt");
    }
    finally
    {
      if (ps != null)
      {
        System.setErr(origErr);
        System.setOut(origOut);
      }
    }
  }
}
