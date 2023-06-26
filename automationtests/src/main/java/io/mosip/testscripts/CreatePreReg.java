package io.mosip.testscripts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.ibm.icu.text.Transliterator;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.dto.OutputValidationDto;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.authentication.fw.util.OutputValidationUtil;
import io.mosip.authentication.fw.util.ReportUtil;
import io.mosip.kernel.util.Translator;
import io.mosip.service.BaseTestCase;
import io.mosip.testrunner.HealthChecker;
import io.mosip.testrunner.MosipTestRunner;
import io.restassured.response.Response;

public class CreatePreReg extends AdminTestUtil implements ITest {
	private static final Logger logger = Logger.getLogger(CreatePreReg.class);
	protected String testCaseName = "";
	public String idKeyName = null;
	public Response response = null;

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		idKeyName = context.getCurrentXmlTest().getLocalParameters().get("idKeyName");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}
	
	

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws AuthenticationTestException
	 * @throws AdminTestException
	 * @throws NoSuchAlgorithmException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO)
			throws AuthenticationTestException, AdminTestException, NoSuchAlgorithmException {
		testCaseName = testCaseDTO.getTestCaseName();
		if (HealthChecker.signalTerminateExecution == true) {
			throw new SkipException("Target env health check failed " + HealthChecker.healthCheckFailureMapS);
		}
		testCaseDTO.setInputTemplate(AdminTestUtil.generateHbsForPrereg(false));
		String[] templateFields = testCaseDTO.getTemplateFields();

		// filterHbs(testCaseDTO);

		// testCaseDTO=AdminTestUtil.filterHbs(testCaseDTO);

		//String inputJson = filterInputHbs(testCaseDTO);
		// String outputJson = filterOutputHbs(testCaseDTO);

		
		String jsonInput = testCaseDTO.getInput();
		
		
		/*
		 * int maxSupportedLang = 10; for(int i=BaseTestCase.languageList.size();
		 * i<maxSupportedLang; i++) { String preFix = ", { \"language\": \"$"; String
		 * suFix = "LANG$\", \"value\": "; int langNumber = i+1;
		 * 
		 * String eleMent ="\"FR\" }"; jsonInput = jsonInput.replace(preFix + langNumber
		 * + suFix+ eleMent, "");
		 * 
		 * eleMent ="\"Test Book appointment\" }"; jsonInput = jsonInput.replace(preFix
		 * + langNumber + suFix+ eleMent, "");
		 * 
		 * eleMent ="\"MLE\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * 
		 * eleMent ="\"RSK\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * eleMent ="\"KTA\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * eleMent ="\"KNT\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * eleMent ="\"BNMR\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * 
		 * eleMent ="\"NFR\" }"; jsonInput = jsonInput.replace(preFix + langNumber +
		 * suFix+ eleMent, "");
		 * 
		 * }
		 */
		
		
		String inputJson = getJsonFromTemplate(jsonInput, testCaseDTO.getInputTemplate(), false);
		String outputJson = getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate());
		// filterOutputHbs(testCaseDTO);

		if (testCaseDTO.getTemplateFields() != null && templateFields.length > 0) {
			ArrayList<JSONObject> inputtestCases = AdminTestUtil.getInputTestCase(testCaseDTO);
			ArrayList<JSONObject> outputtestcase = AdminTestUtil.getOutputTestCase(testCaseDTO);
			// adding...
			List<String> languageList = new ArrayList<>();
			languageList = new ArrayList<String>(BaseTestCase.getLanguageList());
			for (int i = 0; i < languageList.size(); i++) {
//				Innerloop: 
//				for (int j = i; j < languageList.size(); j++) {
					response = postWithBodyAndCookieForAutoGeneratedId(ApplnURI + testCaseDTO.getEndPoint(),
							getJsonFromTemplate(inputtestCases.get(i).toString(), testCaseDTO.getInputTemplate()),
							COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName);

					Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil.doJsonOutputValidation(
							response.asString(),
							getJsonFromTemplate(outputtestcase.get(i).toString(), testCaseDTO.getOutputTemplate()));
					if (testCaseDTO.getTestCaseName().toLowerCase().contains("dynamic")) {
						JSONObject json = new JSONObject(response.asString());
						idField = json.getJSONObject("response").get("id").toString();
					}
					Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));

					if (!OutputValidationUtil.publishOutputResult(ouputValid))
						throw new AdminTestException("Failed at output validation");
//					break Innerloop;
//				}
			}
		} else {
			response = postWithBodyAndCookieForAutoGeneratedId(ApplnURI + testCaseDTO.getEndPoint(), inputJson,
					COOKIENAME, testCaseDTO.getRole(), testCaseDTO.getTestCaseName(), idKeyName);
			Map<String, List<OutputValidationDto>> ouputValid = OutputValidationUtil
					.doJsonOutputValidation(response.asString(), outputJson);
			Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));
			if (!OutputValidationUtil.publishOutputResult(ouputValid))
				throw new AdminTestException("Failed at output validation");
		}

	}

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}
}
