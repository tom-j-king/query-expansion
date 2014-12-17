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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Main {

	public static void main(String[] args) throws Exception {		
		displayQuery();		
	}

	public static void displayQuery() throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
		String provType = "";
		String provNumber;	
		String expandedQuery;
	
		System.out.print("Enter Provision Type: ");
		String inputtedProvisionType = br.readLine();
		
		//Only set up to deal with "section" and "regulation" provision types
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
		
		expandedQuery = expandQuery(provType, provNumber);
		
		System.out.println("Expanded Query Is: " + expandedQuery);
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
			terms.add(prefix + "" + provisionNumber);
			terms.add(prefix +"." + provisionNumber);
			terms.add(quote + prefix + " " + provisionNumber + quote);
			terms.add(quote + prefix + ". " + provisionNumber + quote);
			
			expandedQuery = termJoiner.join(terms);						
		}	
	
		return expandedQuery;			
	}	
}
