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
	
	public static final Pattern PROVISION_SEARCH_PATTERN = Pattern.compile("TI\\(.+?\\)\\s&\\sPT\\(.+?\\)\\s&\\sPR\\(.+?\\)");
	public static final Pattern PROVNUMBER_FIELD_PATTERN = Pattern.compile("PR\\((.+?)\\)");
	public static final Pattern PROVTYPE_FIELD_PATTERN = Pattern.compile("PT\\((.+?)\\)");
	public static final Pattern TITLE_FIELD_PATTERN = Pattern.compile("TI\\((.+?)\\)");
	
	public static final Pattern NON_TITLE_FIELD_PATTERN = Pattern.compile("[A-Z&&[^T]][A-Z&&[^I]]\\(.+?\\)");
	public static final Pattern NON_PROV_SEARCH_FIELD_PATTERN = Pattern.compile("[A-Z&&[^P]][A-Z&&[^T|^R]]\\(.+?\\)");
	
	public static final Pattern NON_RECOG_FIELD_PATTERN = Pattern.compile("[A-Z&&[^T|^P]A-Z&&[^I|^T|^R]]\\(.+?\\)");	
	
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
		String inputtedQuery = br.readLine().trim();		
		
		if (queryIsForExpansion(inputtedQuery))
		{			
			queryComponents(inputtedQuery);
		}				
	}
	
	public static boolean queryIsForExpansion(final String query)
	{
		//Only expand if TI field present
		boolean isForExpansion = false;		
		
		Matcher titleQueryMatcher = TITLE_FIELD_PATTERN.matcher(query);				
		
		if (titleQueryMatcher.find())
		{
			isForExpansion = true;						
		}	
		
		return isForExpansion;
	}
	
	//Assuming title is not "ANDED"can split on " & "
	public static void queryComponents(final String query)
	{
		String str = query;
		String title = null;
		String provType = null;
		String provNumber = null;
		Joiner termJoiner = Joiner.on(" & ");
		List<String> terms = Lists.newArrayList();
		
		for (String component : str.split("\\s&\\s"))
		{			
			if(component.matches("TI\\((.+?)\\)"))
			{
				title = andTitle(component);				
				terms.add(title);
			}
			else if (component.matches("PT\\((.+?)\\)"))
			{
				provType = component;				
			}
			else if (component.matches("PR\\((.+?)\\)"))
			{
				provNumber = component;				
			}
			else
			{
				terms.add(component);
			}						
		}
		
		//Title only query manipulation
		if(terms.size() == 1)
		{
			String singleComponent = terms.get(0);
			if(singleComponent.matches("TI\\((.+?)\\)"))
			{
				terms.add("(PR(\"arrangement of act\" OR \"arrangement of si\" OR \"arrangement of document\"))");
			}
		}
		
		if (provType != null && provNumber != null)
		{
			provType = retrieveQueryFieldValue(provType);
			provNumber = retrieveQueryFieldValue(provNumber);
			final String field = calculateNovusFieldPrefix(provType);			
			final String provSearch = expandQuery(provType, provNumber);
			final String expandedQuery = novusExpandedProvisionPattern
				    .replace("FIELD", field)
				    .replace("expandedProvisionQuery", provSearch);
					
			System.out.println("Expanded Query: " + expandedQuery);
			terms.add(expandedQuery);
		}	
		
		final String expandedQuery = termJoiner.join(terms);
		System.out.println("Expanded Query Is: " + expandedQuery);
	}	
	
	public static String andTitle(final String titleValue)
	{
		final String title = titleValue.replaceAll(" ", " & ");		
		return title;
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
		
		//Error test if if PT is not a valid type if so do nothing
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
		
		String expandedQuery = termJoiner.join(terms);
		if (provisionType.equals("chapter"))
		{
			expandedQuery = expandedQuery + " & (md.infotype(\"legis-AOP\") OR md.infotype(\"legis-AOP-scottish\"))";
		}
	
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
	
	public static String retrieveQueryFieldValue(final String query)
	{
		String fieldValue = query;
		final int openParenthesis = fieldValue.indexOf("(");
		final int closeParenthesis = fieldValue.lastIndexOf(")");
		
		fieldValue = fieldValue.substring(openParenthesis + 1, closeParenthesis);
		
		return fieldValue;
	}		
}
