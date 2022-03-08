package se.uu.ub.cora.diva.mixedstorage.id;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpy implements HttpHandlerFactory {
	public List<String> urls = new ArrayList<>();
	public List<HttpHandlerSpy> factoredHttpHandlers = new ArrayList<>();
	public List<Integer> responseCodes = new ArrayList<>();
	public List<String> responseTexts = new ArrayList<>();

	@Override
	public HttpHandler factor(String url) {
		urls.add(url);
		HttpHandlerSpy httpHandlerSpy = new HttpHandlerSpy();
		factoredHttpHandlers.add(httpHandlerSpy);

		int numOfHandlersFactored = factoredHttpHandlers.size();

		httpHandlerSpy.responseText = responseTexts.get(numOfHandlersFactored - 1);
		httpHandlerSpy.responseCode = responseCodes.get(numOfHandlersFactored - 1);
		return httpHandlerSpy;
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
