package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import it.uniroma2.isw2.deliverable2.entities.Version;

public class JIRAHelper {
		
	private String projectName;
	
	public JIRAHelper(String projectName) {
		this.projectName = projectName;
	}
	
	private List<Version> getAllVersionsByJSON() throws JsonSyntaxException, IOException {
		List<Version> allVersions = new ArrayList<>();
		
		final String URL = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
		JsonArray jsonVersions = RestHelper.getJSONObject(URL).get("versions").getAsJsonArray();
		jsonVersions.forEach(element -> {
			JsonObject jsonVersion = element.getAsJsonObject();
			LocalDateTime date = LocalDate.parse(jsonVersion.get("releaseDate").getAsString()).atStartOfDay();
			String id = jsonVersion.get("id").getAsString();
			String name = jsonVersion.get("name").getAsString();
			
			SimpleLogger.logInfo("New version inserted [ID: {0}, Name: {1}, Date: {2}]",
					new Object[] {id, name, date});
			
			Version version = new Version(id, name, date);
			allVersions.add(version);
		});
		
		allVersions.sort((v1, v2) -> v1.getStartDate().compareTo(v2.getStartDate()));
		SimpleLogger.logInfo("Founded {0} versions", allVersions.size());
		
		return allVersions;
	}
	
	public List<Version> getVersions() throws IOException {		
		List<Version> consideredVersions = new ArrayList<>();
		List<Version> allVersions = this.getAllVersionsByJSON();
		
		for (int i=0; i<(int)Math.ceil(allVersions.size()/2.0); ++i) {
			Version current = allVersions.get(i);
			Version next = allVersions.get(i+1);
			
			current.setEndDate(next.getStartDate());
			consideredVersions.add(current);
		}		
		
		SimpleLogger.logInfo("Considered versions: {0}", consideredVersions.size());
		return consideredVersions;
	}
}