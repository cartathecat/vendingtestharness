package app.com.vending.machine.testharness;

/**
 * Vending Machine Testharness
 * 
 * March 2022
 * 
 * Simple test harness to test Vending Machine microservice
 * 
 */
import java.io.IOException;
import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class VendingTestHarness {

	private static CloseableHttpResponse response;
	
	private static int portNumber;
	private static int timeoutValue;

	public static void setPortNumber(int port) {
		portNumber = port;
	}
	public static int getPortNumber() {
		return portNumber;
	}
	public static void setTimeOutValue(int t) {
		timeoutValue = t;
	}
	public static int getTimeOutValue() {
		return timeoutValue;
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {

		boolean invalidCommands = ParseArgs(args);
		if (invalidCommands) {
			System.out.println("Error parsing commandline " );
			Help();
			System.exit(1);
		}
		Menu();
		
	}

	/** 
	 * Parse passed in params
	 * 
	 * @param args
	 * @return
	 */
	private static boolean ParseArgs(String[] args) {

		setTimeOutValue(5);
		
		int invalidArgument = 1;
		boolean invalidPos = false;
		String parg;

		int counter = 0;
		try {
			for (counter = 0; ((counter < args.length) && !invalidPos); counter++) {
				
				parg = args[counter];
				if (!parg.startsWith("-") && !parg.startsWith("/")) {
					invalidPos = true;
					invalidArgument = counter+1;
					continue;
				}
				
				switch (parg) {
				
				case "-p":
				case "--port":
					counter++;
					int port = Integer.parseInt(args[counter]);
					setPortNumber(port);
					invalidArgument = 0;
					break;
				case "-t":
				case "--timeout":
					counter++;
					int timeout = Integer.parseInt(args[counter]);
					setTimeOutValue(timeout);
					invalidArgument = 0;
					break;
					
				case "-h":
				case "/?":
					invalidPos = true;
					break;
					
				default:
					invalidPos = true;
					invalidArgument = counter;
					break;
				
				
				}
			}
			if (invalidPos) {
				System.out.println(String.format("Argument %d has an error", invalidArgument));
			}
		} catch (Exception e) {
			invalidArgument = counter;			
		}
		return invalidPos;
		
		
	}

	/**
	 * Help
	 */
	private static void Help() {
		System.out.println("");
		System.out.println("Vending Machine Test Harness");
		System.out.println("----------------------------");
		System.out.println("");
		System.out.println("java -jar VendingTestHardness-1.0.0.0.jar");
		System.out.println("Usage [-p      Port Number]       # Port number of the listening Vending Machine service");
		System.out.println("      [--port             ]");
		System.out.println("      [-t                 ]       # Timeout value for API calls");
		System.out.println("      [--timeout          ]");
	}
	
	/**
	 * Show menu and accept input 
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void Menu() throws ClientProtocolException, IOException {		

		String option = "";
		Scanner scanner = new Scanner(System.in);

		//static String vendingStatus = "";
		
		while (!option.equals("E")) {

			System.out.println("--------------------------------------------------------");
			System.out.println("");

			String vendingStatus = GetStatus();
			System.out.println("GetStatus response: " + vendingStatus);
			
			System.out.println("");
			System.out.println("Vending Test Harness");
			System.out.println("--------------------");
			System.out.println("I - Initialise a Vending machine");
			System.out.println("--------------------------------");
			System.out.println("D - Deposit coins before vending");
			System.out.println("V - Vend");
			System.out.println("R - Refund");
			System.out.println("--------------------------------");			
			System.out.println("P - Show products");
			System.out.println("C - Show Coin bucket");
			System.out.println("F - Show Float / Deposit values");
			System.out.println("E - Exit");
			
			option = scanner.nextLine();
		
			System.out.println("You entered :" + option);
			if (option.length() > 0) {
				switch (option.charAt(0)) {
				
				case 'I':
					Initialize();
					break;
				case 'P':
					ShowProducts();
					break;
				case 'D':
					Deposit();
					break;
				case 'V':
					Vend();
					break;
				case 'R':
					Refund();
					break;
				case 'F':
					GetFloat();
					break;
				case 'C':
					GetCoinBucket();
					break;
				case 'E':
					System.out.println("Exiting");
					break;
					
				default:
					System.out.println("Invalid option ");
					break;
				}
			} else {
				System.out.println("Invalid option ");
				
			}
		}
		scanner.close();
	}


	/**
	 * Get current vending machine status
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static String GetStatus() throws ClientProtocolException, IOException {
		System.out.println("GetStatus");
		
		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
		  .setConnectTimeout(timeout * 1000)
		  .setConnectionRequestTimeout(timeout * 1000)
		  .setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = 
		  HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		
		String result = "";
		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/status",getPortNumber());
		
		HttpGet httpGet = new HttpGet(endpoint);
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {
			System.out.println("Unable to invoke /status endpoint");
			return "TIMEOUT";
		}
		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
			System.out.println("result : " + result);

		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
			result = "ERROR";
		}

		return result;
	}

	/**
	 * Initialise vending machine
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private static void Initialize() throws IOException {
		System.out.println("Initialising Vending Machine");

		String vendingStatus = GetStatus();
		JSONObject jsonObject = new JSONObject(vendingStatus);
		System.out.println("Status: " + jsonObject.getString("status"));
		if (!jsonObject.getString("status").equals("INACTIVE")) {
			System.out.println("Vending machine is already initialised, continuing will produce error response from API");
		}
		
		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String defaultCoins = "TWOPOUND:5,ONEPOUND:10,FIFTY:10,TWENTY:10,TEN:20,FIVE:20,TWO:20,ONE:20";
		System.out.println("Default is : " + defaultCoins);

		String coins = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter float coins :");
		coins = scanner.nextLine();
		System.out.println("You entered :" + coins);
		if (coins.equals("")) {
			coins = defaultCoins;
		}

		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/init/%s",getPortNumber(),coins);
		
		HttpPost httpPost = new HttpPost(endpoint);
		try {
			response = client.execute(httpPost);
		} catch (Exception e) {
			System.out.println("Unable to invoke /init endpoint");
			return;
		}
		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);
		
	}
	
	/**
	 * Show products
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static void ShowProducts() throws ClientProtocolException, IOException {
		System.out.println("Show products");

		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/products",getPortNumber());
		
		HttpGet httpGet = new HttpGet(endpoint);
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {
			System.out.println("Unable to invoke /products endpoint");
			return;
		}
		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			System.out.println("result : " + result);
			
			JSONArray jsonArray = new JSONArray(result);
			String header = String.format("%1$5s %2$-40s %3$-15s %4$5s"
					, "Id"
					, "Product"
					, "Price"
					, "Qty");
			System.out.println();
			System.out.println(header);

			for (Object key : jsonArray) {
				JSONObject tmp = (JSONObject) key;
				
				int price = tmp.getInt("price");
				String line = String.format("%1$5d %2$-40s %3$-15.2f %4$5d"
						, tmp.getInt("id")
						, tmp.getString("description").trim()
						,(double)price/100
						, tmp.getInt("quantityCount"));
				System.out.println(line);
				
			}
		} else {
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity);
			System.out.println("result : " + result);
			
		}
	}

	/**
	 * Deposit money
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static void Deposit() throws ClientProtocolException, IOException {
		System.out.println("Deposit");

		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String coins = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter coins :");
		coins = scanner.nextLine();
		System.out.println("You entered :" + coins);
		
		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/deposit/%s",getPortNumber(),coins);
		
		HttpPost httpPost = new HttpPost(endpoint);
		try {
			response = client.execute(httpPost);
		} catch (Exception e) {
			System.out.println("Unable to invoke /deposit endpoint");
			return;
		}
		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);
	
	}

	/**
	 * Vend products
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void Vend() throws ClientProtocolException, IOException {
		System.out.println("Vend");

		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String product = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter product :");
		product = scanner.nextLine();
		System.out.println("You entered :" + product);
		
		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/vend/%s",getPortNumber(),product);
		
		HttpPut httpPut = new HttpPut(endpoint);
		try {
			response = client.execute(httpPut);
		} catch (Exception e) {
			System.out.println("Unable to invoke /vend endpoint");
			return;
		}

		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);
		
	}

	/**
	 * Refund money
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void Refund() throws ClientProtocolException, IOException {
		System.out.println("Refund");

		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/refund",getPortNumber());
		
		HttpGet httpGet = new HttpGet(endpoint);
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {
			System.out.println("Unable to invoke /refund endpoint");
			return;
		}
		
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);
		
	}

	/**
	 * Get float and depoit values
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void GetFloat() throws ClientProtocolException, IOException {
		System.out.println("GetFloat / Deposit");
		
		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/floatvalue",getPortNumber());

		HttpGet httpGet = new HttpGet(endpoint);
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {
			System.out.println("Unable to invoke /floatvalue endpoint");
			return;
		}
	
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);

	}

	/**
	 * Get coin bucket
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void GetCoinBucket() throws ClientProtocolException, IOException {
		System.out.println("GetCoinBucket");

		int timeout = getTimeOutValue();
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

		String endpoint = String.format("http://localhost:%s/vendingmachine/v1/coinbucket",getPortNumber());
		
		HttpGet httpGet = new HttpGet(endpoint);
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {
			System.out.println("Unable to invoke /coinbucket endpoint");
			return;
		}
	
		System.out.println("response.getStatusLine() :: " + response.getStatusLine());
		final int statusCode = response.getStatusLine().getStatusCode();	
		if (statusCode == HttpStatus.SC_OK) {
			System.out.println("Status Code  : " + HttpStatus.SC_OK);
		} else {
			System.out.println("StatusCode not 200  : " + statusCode);
		}
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		System.out.println("result : " + result);

	}

}
