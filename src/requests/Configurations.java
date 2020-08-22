package requests;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Random;

// Singleton class
class Configurations {
	
	// The possible urls for sdarot website 
	private final String[] SDAROT_URLS 
	= {"https://sdarot.rocks",
			//"https://www.hasdarot.net", // This one has different api - by show name not by id
			"http://sdarot.pro", 
			"https://sdarot.world",
			"https://sdarot.tv", 
			"https://sdarot.work" };
	// sdarot website page can't contain this
	private final String[] WEBSITE_NOT_CONTAINES = {"אתר זה הינו אתר מפר זכויות יוצרים"};
	// Some options for user agent
	private final String[] USER_AGENTS 
	= {"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36", 
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:76.0) Gecko/20100101 Firefox/76.0" };
	// x requested 
	public final String X_REQUESTED_WITH = "XMLHttpRequest";
	public final String CONTENT_TYPE= "application/x-www-form-urlencoded";
	
	// Delay before episode in milliseconds
	public final int PRE_WATCH_DELAY_TIME = 30000;

	// the sdarot uri
	private URI sdarotURI;
	// the watch uri
	private URI watchURI;
	// reusing the client for all requests
	private HttpClient httpClient;
	// user-agent header (The browser agent)
	private String userAgent;

	private static Configurations instance = null;
	
	public static Configurations getInstance() {
		if(instance==null) {
			instance = new Configurations();
		}
		return instance;
	}
	
	private Configurations() {
		// getting random user-agent (browser)
		this.userAgent = USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
		
		// making the default cookieHandler create a cookie manager which will handle the cookies
	    CookieHandler.setDefault(new CookieManager());

	    // because of cookie Handler the cookie handling is transparent for this client
		httpClient = HttpClient.newBuilder()
	            .version(HttpClient.Version.HTTP_2)
	            .cookieHandler(CookieHandler.getDefault())
	            .build();
		
		setAvailableURL();
		
		if (this.getSdarotURI() == null) {
			throw new NullPointerException("We could not find any sdarot site to access");
		}
	}
	
	public URI getSdarotURI() {
		return this.sdarotURI;
	}
	
	public URI getWatchURI() {
		return this.watchURI;
	}
	
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public String getUserAgent() {
		return this.userAgent;
	}
	
	private void setAvailableURL() {
		HttpResponse<String> response;
		HttpRequest request;
		URI uri;
		this.sdarotURI = null;
		
		for (String url : SDAROT_URLS) {
			uri = URI.create(url);
	        request = HttpRequest.newBuilder()
	                .GET()
	                .uri(uri)
	                .setHeader("User-Agent", this.userAgent)
	                .build();

			try {
				response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		        
		       if (response.statusCode() == HTTPStatus.OK ) {
		    	   // we got good response - now need to check the page is what we expect
		    	   boolean goodUri = true;
		    	   for (String s : WEBSITE_NOT_CONTAINES) {
		    		   if(response.body().contains(s)) {
		    			  goodUri = false;
		    			  break;
		    		   }
		    	   }
		    	   
		    	   if (goodUri) {
		    		   this.sdarotURI = uri;
		    		   this.watchURI = URI.create(String.format("%s%s",uri.toString(), "/ajax/watch")).normalize();
		    		   break;
		    	   }
		       }
			} catch (IOException | InterruptedException e1) {
				System.out.printf("%s is not valid%n", url);
				e1.printStackTrace();
			}
		}
	}
}