/* 
 * Basic class to illustrate expanding a provision query for Legislation Advanced Query
 * 
 * Uses map containing prefixes for each provision type to expand out the provision query
 * 
 *  Added comment to test git commit from local machine
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Main {
	
	public static final Pattern PROVISION_SEARCH_PATTERN = Pattern.compile("TI\\(.+?\\) & PT\\(.+?\\) & PR\\(.+?\\)");
	public static final Pattern PROVNUMBER_FIELD_PATTERN = Pattern.compile("PR\\((.*?)\\)");
	public static final Pattern PROVTYPE_FIELD_PATTERN = Pattern.compile("PT\\((.*?)\\)");
	public static final Pattern TITLE_FIELD_PATTERN = Pattern.compile("TI\\((.*?)\\)");
	
	private static final String novusTitlePattern = "TI(titleValue)";
	private static final String novusExpandedProvisionPattern = "FIELD(expandedProvisionQuery)";
	
	public static void main(String[] args) throws Exception {		
		displayQuery();		
	}

	//method here for ability to run query expansion method via console window to speed up development
	public static void displayQuery() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Set<String> recognizedProvisionTypes = ImmutableSet.of(
				"article",
				"chapter",
				"paragraph",
				"part",
				"regulation",
				"rule",
				"section",
				"schedule");		
			
		System.out.print("Enter Legislation Query: ");		
		//String inputtedProvisionType = br.readLine();
		String inputtedQuery = br.readLine();		
		
		//need to check form of query to cater for user directly entering advanced query from the search box, bypassing the form - front end?
		
		String titleValue = "";		
		String provTypeValue = "";		
		String provNumberValue = "";
		
		//need to catch with title but no correctly other defined fields. don't want to do anything with this query
		if(!inputtedQuery.matches("TI\\(.+?\\) & PT\\(.+?\\) & PR\\(.+?\\)"))
		{
			System.out.println("valid query for expansion");
		}
		if(inputtedQuery.matches("TI\\(.+?\\)"))
		{			
			titleValue = retrieveQueryFieldValue(TITLE_FIELD_PATTERN, inputtedQuery);
			System.out.println("i have a title: " + titleValue);
		}
		if(inputtedQuery.matches(".+?PT\\(.+?\\)"))
		{
			provTypeValue = retrieveQueryFieldValue(PROVTYPE_FIELD_PATTERN, inputtedQuery);
			
		}
		if(inputtedQuery.matches(".+?PR\\(.+?\\)"))
		{
			provNumberValue = retrieveQueryFieldValue(PROVNUMBER_FIELD_PATTERN, inputtedQuery);
			System.out.println("i have a prov number: " + provNumberValue);
		}		
		
		//if either PT or PR missing just return the title as the query
		if(provTypeValue.isEmpty() || provNumberValue.isEmpty())
		{
			//System.out.print("Please run again inputting a valid provision type.");
			//System.exit(1);
			final String expandedQuery = novusTitlePattern.replace("titleValue", titleValue);
			System.out.println("Expanded Query Is: " + expandedQuery);
		}
		else
		{
			final String expandedProvisionQuery = expandQuery(provTypeValue, provNumberValue);
			final String field = calculateNovusFieldPrefix(provTypeValue);
			final String expandedQuery = 
					novusTitlePattern.replace("titleValue", titleValue) 
					    + " & " + novusExpandedProvisionPattern
					    .replace("FIELD", field)
					    .replace("expandedProvisionQuery", expandedProvisionQuery);
			System.out.println("Expanded Query Is: " + expandedQuery);
		}
		
		/*if (recognizedProvisionTypes.contains(value))
		{			
			final String provType = inputtedProvisionType;
			
			System.out.print("Enter Provision Number: ");
			String inputtedProvisionNumber = br.readLine();
			final String provNumber = inputtedProvisionNumber;
			
			System.out.print("Enter full query sting: ");
			String inputtedQueryString = br.readLine();			
			
			final String expandedQuery = expandQuery(provType, provNumber);
			
			System.out.println("Expanded Query Is: " + expandedQuery);									
		}
		else
		{
			System.out.print("Please run again inputting a valid provision type.");
			System.exit(1);
		}*/		
	}

	public static String expandQuery(final String provisionType, final String provisionNumber) {				
	
		//map would be injected as xml bean
		final Map<String, List> provTypes = Maps.newHashMap();			
		List<String> articlePrefixes = Arrays.asList("article", "art");
		List<String> chapterPrefixes = Arrays.asList("chapter");
		List<String> paragraphPrefixes = Arrays.asList("paragraph", "para");
		List<String> partPrefixes = Arrays.asList("part", "par", "pt", "p", "schpart");
		List<String> regulationPrefixes = Arrays.asList("regulation", "reg", "r");
		List<String> rulePrefixes = Arrays.asList("rule", "r");
		List<String> sectionPrefixes = Arrays.asList("section", "s");		
		List<String> schedulePrefixes = Arrays.asList("schedule", "sch");		
		
		provTypes.put("article", articlePrefixes);
		provTypes.put("chapter", chapterPrefixes);
		provTypes.put("paragraph", paragraphPrefixes);
		provTypes.put("part", partPrefixes);
		provTypes.put("regulation", regulationPrefixes);
		provTypes.put("rule", rulePrefixes);
		provTypes.put("section", sectionPrefixes);
		provTypes.put("schedule", schedulePrefixes);		
		
		final String quote = "\"";		
		Joiner termJoiner = Joiner.on(" OR ");
		
		List<String> currentPrefixes = provTypes.get(provisionType);
		List<String> terms = Lists.newArrayList();
		
		for (String prefix : currentPrefixes)
		{			
			//pre-formed version of the query
			terms.add(prefix + "" + provisionNumber);
			terms.add(prefix + "." + provisionNumber);
			terms.add(quote + prefix + "." + provisionNumber + quote);
			terms.add(quote + prefix + " " + provisionNumber + quote);
			terms.add(quote + prefix + ". " + provisionNumber + quote);									
		}
		
		final String expandedQuery = termJoiner.join(terms);
	
		return expandedQuery;			
	}
	
	public static String calculateNovusFieldPrefix(final String provisionType)
	{
		String novusFieldPrefix = "PR";
		Set<String> caProvisionTypes = ImmutableSet.of(				
				"article",
				"chapter",				
				"part");
		
		if(caProvisionTypes.contains(provisionType))
		{
			novusFieldPrefix = "CA";
		}
		
		return novusFieldPrefix;
	}
	
	public static String retrieveQueryFieldValue(final Pattern pattern, final String query)
	{
		String fieldValue = query;		
		Matcher matcher = pattern.matcher(query);
		if (matcher.find())
		{
			fieldValue = matcher.group(1);
		}		
		return fieldValue;
	}
	
	public void addAdditionalFields(final String provType, final String expandedQuery)
	{
		//add info type field etc
	}	
}
