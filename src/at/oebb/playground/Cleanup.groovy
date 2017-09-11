import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.common.app.GlobalComponentLookupHelper
import org.sonatype.nexus.repository.maintenance.MaintenanceService
import org.sonatype.nexus.repository.storage.ComponentMaintenance
import org.sonatype.nexus.repository.storage.Query;
import org.sonatype.nexus.script.plugin.RepositoryApi
import org.sonatype.nexus.script.plugin.internal.provisioning.RepositoryApiImpl
import com.google.common.collect.ImmutableList
import org.joda.time.DateTime;
import org.slf4j.Logger

// ----------------------------------------------------
// delete these rows when this script is added to nexus
RepositoryApiImpl repository = null;
Logger log = null;
GlobalComponentLookupHelper container = null;
// ----------------------------------------------------

def retentionDays = 30;
def retentionCount = 10;
def repositoryName = 'maven-releases';
def whitelist = ["org.javaee7.sample/javaee7-simple-sample", "org.javaee7.next/javaee7-another-sample"].toArray();


log.info(":::Cleanup script started!");
MaintenanceService service = container.lookup("org.sonatype.nexus.repository.maintenance.MaintenanceService");
def repo = repository.repositoryManager.get(repositoryName);
def tx = repo.facet(StorageFacet.class).txSupplier().get();
def components = null;
try {
	tx.begin();
	components = tx.browseComponents(tx.findBucket(repo));
}catch(Exception e){
	log.info("Error: "+e);
}finally{
	if(tx!=null)
		tx.close();
}

if(components != null) {
	def retentionDate = DateTime.now().minusDays(retentionDays).dayOfMonth().roundFloorCopy();
	int deletedComponentCount = 0;
	int compCount = 0;
	def listOfComponents = ImmutableList.copyOf(components);
	def previousComp = listOfComponents.head().group() + listOfComponents.head().name();
	listOfComponents.reverseEach{comp ->
		log.info("Processing Component - group: ${comp.group()}, ${comp.name()}, version: ${comp.version()}");
		if(!whitelist.contains(comp.group()+"/"+comp.name())){
			log.info("previous: ${previousComp}");
			if(previousComp.equals(comp.group() + comp.name())) {
				compCount++;
				log.info("ComCount: ${compCount}, ReteCount: ${retentionCount}");
				if (compCount > retentionCount) {
					log.info("CompDate: ${comp.lastUpdated()} RetDate: ${retentionDate}");
					if(comp.lastUpdated().isBefore(retentionDate)) {
						log.info("compDate after retentionDate: ${comp.lastUpdated()} isAfter ${retentionDate}");
						log.info("deleting ${comp.group()}, ${comp.name()}, version: ${comp.version()}");
						
						// ------------------------------------------------
						// uncomment to delete components and their assets
						// service.deleteComponent(repo, comp);
						// ------------------------------------------------
						
						log.info("component deleted");
						deletedComponentCount++;
					}
				}
			} else {
				compCount = 1;
				previousComp = comp.group() + comp.name();
			}
		}else{
			log.info("Component skipped: ${comp.group()} ${comp.name()}");
		}
	}

	log.info("Deleted Component count: ${deletedComponentCount}");
}
