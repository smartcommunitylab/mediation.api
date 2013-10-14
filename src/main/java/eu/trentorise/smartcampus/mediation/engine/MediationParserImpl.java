package eu.trentorise.smartcampus.mediation.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import eu.trentorise.smartcampus.mediation.model.CommentBaseEntity;
import eu.trentorise.smartcampus.mediation.model.MessageToMediationService;
import eu.trentorise.smartcampus.mediation.util.MediationConstant;
import eu.trentorise.smartcampus.mediation.util.TextReader;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteException;

public class MediationParserImpl {
	
	private DataSource dataSource;
	private String urlServermediation;
	private String webappname;
	private String client_token="blabla";
	
	private static final Logger logger = Logger
			.getLogger(MediationParserImpl.class);
	
	public MediationParserImpl(){}

	public MediationParserImpl(DataSource dataSource,String urlServermediation,String webappname){
		this.dataSource=dataSource;	
		this.setUrlServermediation(urlServermediation);
		this.webappname=webappname;
	}
	
	
	public boolean[] validateComments(List<CommentBaseEntity> entities){
		boolean[] returnApprovedState=new boolean[entities.size()];
		
		int i=0;
		for(CommentBaseEntity index : entities){
			returnApprovedState[i]=validateComment(index);
			i++;
		}
		
		return returnApprovedState;
		
		
	}
	
	public boolean validateComment(CommentBaseEntity entity){
		
		MessageToMediationService messageToMediationService=new MessageToMediationService(webappname,entity.getId(),entity.getTesto());
		Collection<String> x=getNotApprovedWordDictionary();
		Iterator<String> index=x.iterator();
		
		boolean isApproved=true;
		
		while(index.hasNext() && isApproved){	
			Calendar cal = Calendar.getInstance();
			long before = cal.getTimeInMillis();
			String test=index.next();
			isApproved=(entity.getTesto().indexOf(test)==-1);
			if(!isApproved)	{	
				messageToMediationService.setParseApproved(isApproved);
				addCommentToMediationService(messageToMediationService);
				long after = cal.getTimeInMillis();
				logger.info("Time parsing = "+(after-before)+" millisec");
				return isApproved;		
			}
		}
				
		
		
		messageToMediationService.setParseApproved(isApproved);		
		addCommentToMediationService(messageToMediationService);
		
		
		return isApproved;
		
		
	}
	
	
	
	
	private void addCommentToMediationService(
			MessageToMediationService messageToMediationService)  {
		
		
		try {
			RemoteConnector.postJSON(urlServermediation, MediationConstant.ADD_COMMENT, messageToMediationService.ToJson(), client_token);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	// restituisce la lista delle parole da filtrare
	private Collection<String> getNotApprovedWordDictionary() {
		Collection<String> stringColl=new ArrayList<String>();
		
		TextReader readerBW = new TextReader();
		stringColl = readerBW.getListFromFiles();
		
		return stringColl;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getUrlServermediation() {
		return urlServermediation;
	}

	public void setUrlServermediation(String urlServermediation) {
		this.urlServermediation = urlServermediation;
	}

	public String getWebappname() {
		return webappname;
	}

	public void setWebappname(String webappname) {
		this.webappname = webappname;
	}

}
