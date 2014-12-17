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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Main {

	public static final Pattern PROVNUMBER_FIELD_PATTERN = Pattern.compile("PR\\((.*?)\\)");
	public static final Pattern PROVTYPE_FIELD_PATTERN = Pattern.compile("PT\\((.*?)\\)");
	public static final Pattern TITLE_FIELD_PATTERN = Pattern.compile("TI\\((.*?)\\)");
	
	public static void main(String[] args) throws Exception {		
		displayQuery();		
	}

	//method here for ability to run query expansion method via console window to speed up development
	public static void displayQuery() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
		String provType = "";
		String provNumber;	
		String expandedQuery;
	
		System.out.print("Enter Provision Type: ");
		String inputtedProvisionType = br.readLine();		
		
		if (inputtedProvisionType.equals("article") || inputtedProvisionType.equals("chapter") || inputtedProvisionType.equals("paragraph")
				|| inputtedProvisionType.equals("part") || inputtedProvisionType.equals("regulation") || inputtedProvisionType.equals("rule")
				|| inputtedProvisionType.equals("section") || inputtedProvisionType.equals("schedule"))
		{			
			provType = inputtedProvisionType;
		}
		else
		{
			System.out.print("Please run again inputting a valid provision type.");
			System.exit(1);
		}	
		
		System.out.print("Enter Provision Number: ");
		String inputtedProvisionNumber = br.readLine();
		provNumber = inputtedProvisionNumber;
		
		System.out.print("Enter full query sting: ");
		String inputtedQueryString = br.readLine();		
		String titleField = retrieveQueryFieldValues(TITLE_FIELD_PATTERN, inputtedQueryString);
		String provTypeField = retrieveQueryFieldValues(PROVTYPE_FIELD_PATTERN, inputtedQueryString);
		String provNumberField = retrieveQueryFieldValues(PROVNUMBER_FIELD_PATTERN, inputtedQueryString);
		
		expandedQuery = expandQuery(provType, provNumber);
		
		System.out.println("Expanded Query Is: " + expandedQuery);
		System.out.println("Title Field Value Is: " + titleField);
		System.out.println("Type Field Value Is: " + provTypeField);
		System.out.println("Number Field Value Is: " + provNumberField);
	}

	public static String expandQuery(final String provisionType, final String provisionNumber) {				
	
		//map would be injected as xml bean
		final Map<String, List> provTypes = new HashMap<String, List>();			
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
		String expandedQuery = "";
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
			
			expandedQuery = termJoiner.join(terms);						
		}	
	
		return expandedQuery;			
	}
	
	public static String retrieveQueryFieldValues(final Pattern pattern, final String query)
	{
		String value = "";
		Matcher matcher = pattern.matcher(query);
		if (matcher.find())
		{
			value = matcher.group(1);
		}
		else
		{
			value = "Didn't work";
		}
		return value;
	}
	
	public void addAdditionalFields()
	{
		//add info type field etc
	}
	
	public void changeFieldName()
	{
		// amend field value for certain provision types
	}
}
