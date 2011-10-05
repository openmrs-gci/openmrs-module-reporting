package org.openmrs.module.reporting.cohort;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;

/**
 * A utility class for cohort
 */
public class CohortUtil {
	
	protected static Log log = LogFactory.getLog(CohortUtil.class);

	public static Cohort intersectNonNull(Cohort...cohorts) {
		Cohort ret = null;
		for (Cohort c : cohorts) {
			if (c != null) {
				if (ret == null) {
					 ret = new Cohort(c.getMemberIds());
				}
				else {
					ret = Cohort.intersect(ret, c);
				}
			}
		}
		return ObjectUtil.nvl(ret, new Cohort());
	}
	
	/**
	 * 
	 * @param cohort
	 * 			The cohort to be limited.
	 * @param size
	 * 			The desired size of the cohort.
	 */
	public static Cohort limitCohort(Cohort cohort, int size) { 		
		Cohort limitCohort = new Cohort();
		
		if (cohort != null && !cohort.isEmpty()) { 	
			if (cohort.getSize() <= size) 
				return cohort;	
			int count = 0;
			for (Integer memberId : cohort.getMemberIds()) { 			
				if (count++ < size) 
					limitCohort.addMember(memberId);
				else 
					break;					
			}			
		}
		return limitCohort;
	}
	
	
    /**
     * Returns a cohort of patients of the given size.
     *       
     * @param size
     * 		the desired size of the cohort 
     * @return	
     * 		a cohort of patients 
     */
    public static Cohort getRandomCohort(Integer size) { 
    	
    	Integer cohortSize = 
    		(size != null && size > 0) ? size 
    				: Integer.parseInt(Context.getAdministrationService().getGlobalProperty("reporting.preview.cohortSize", "100"));
    	
		Cohort randomCohort = new Cohort();
    
		Cohort tempCohort = Context.getPatientSetService().getAllPatients();

		Random random = new Random();
		
		// Convert patient IDs to a list
		List<Integer> patientIds = Arrays.asList(tempCohort.getMemberIds().toArray(new Integer[0]));
		if (tempCohort != null && !tempCohort.isEmpty()) { 

			// If the "all patients" cohort is less than or equal to the desired cohort size
			// then we just use the available "all patients" cohort.
			if (tempCohort.getMemberIds().size()<=cohortSize) { 
				randomCohort = tempCohort;
			} 
			// Otherwise we create a random cohort 
			else { 
				for(int i=0; i<cohortSize; i++) {					
					Integer randomIndex = random.nextInt(tempCohort.getSize());
					Integer patientId = patientIds.get(randomIndex);
					// TODO We need to deal with patients that have already been selected
					//  because we don't want duplicates.  This requires special handling
					// since we want to have exactly "cohortSize" patients in the cohort.					
					randomCohort.addMember(patientId);
				}
			}
		}		
		return randomCohort;
    	
    }	
	
    /**
     * @return true if the passed Cohorts have the same members
     */
    public static boolean areEqual(Cohort a, Cohort b) {
    	return (a != null && b != null && a.size() == b.size() && a.size() == Cohort.intersect(a, b).size());
    }
	
}
