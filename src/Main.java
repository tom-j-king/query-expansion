/* 
 * Basic class to illustrate expanding a provision query for Legislation Advanced Query
 * 
 * Uses map containing prefixes for each provision type to expand out the provision query  
 *  
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		if (inputtedProvisionType.equals("section") || inputtedProvisionType.equals("regulation"))
		{			
			provType = inputtedProvisionType;
		}
		else
		{
			System.out.print("Please run again inputting either \"section\" or \"regulation\" for provision type");
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
		final Map<String, Object> provTypes = new HashMap<String, Object>();			
		final String[] sectionPrefixes = {"section", "s"};
		final String[] regulationPrefixes = {"regulation", "reg", "r"};
		provTypes.put("section", sectionPrefixes);
		provTypes.put("regulation", regulationPrefixes);				
		
		final String or = " OR ";
		final String quote = "\"";
		String expandedQuery = "";
		
		List<String> currentPrefixes = Arrays.asList((String[]) provTypes.get(provisionType));	
	
		//need to find last value in List
		for (int i = 0; i < currentPrefixes.size(); i++)
		{			
			String prefix = currentPrefixes.get(i);
			//Pre-formed permutations of the query
			String noSpace = prefix + "" + provisionNumber + or;
			String noSpacePoint = prefix +"." + provisionNumber + or;
			String spaceOnly = quote + prefix + " " + provisionNumber + quote + or;
			String spacePoint = quote + prefix + ". " + provisionNumber + quote;
			
			//don't concatenate " or " to last prefix
			if (i < currentPrefixes.size() - 1)
			{
				spacePoint = spacePoint + or;
			}
			
			expandedQuery = expandedQuery + noSpace + noSpacePoint + spaceOnly + spacePoint;			
		}	
	
		return expandedQuery;			
	}	
}
