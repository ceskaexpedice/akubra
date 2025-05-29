![Build](https://img.shields.io/github/actions/workflow/status/ceskaexpedice/akubra/gradle-push.yml?branch=main)
# Akubra
Akubra repository module is used by Java applications utilizing Fedora/Akubra repositories for ingesting and querying its content.
[`ceskaexpedice/kramerius`](https://github.com/ceskaexpedice/kramerius) is an example of such application.

## Locks server 
Akubra repository needs [`ceskaexpedice/hazelcast-locks-server`](https://github.com/ceskaexpedice/hazelcast-locks-server) running for synchronizing access to documents in the concurrent environment.

## AkubraRepository interface
This interface is the only way how to access all available functionality. 
See the main interface class [`AkubraRepository`](src/main/java/org/ceskaexpedice/akubra/AkubraRepository.java).

## Configuration
In order to use this module in your application you need to obtain a singleton object implementing AkubraRepository interface and provide all necessary configuration parameters. 
This object will then be shared accross your code and used for ingestion and querying data. Here is an example from Kramerius:
```
public class AkubraRepositoryProvider implements Provider<AkubraRepository> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AkubraRepositoryProvider.class.getName());

    @Override
    public AkubraRepository get() {
        String objectPath = KConfiguration.getInstance().getProperty("objectStore.path");
        String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");
        String datastreamStorePath = KConfiguration.getInstance().getProperty("datastreamStore.path");
        String datastreamStorePattern = KConfiguration.getInstance().getProperty("datastreamStore.pattern");

        String solrProcessingHost = KConfiguration.getInstance().getSolrProcessingHost();

        File hazelcastConfigFile = KConfiguration.getInstance().findConfigFile("hazelcast.clientconfig");
        String hazelcastConfigFileS = (hazelcastConfigFile != null && hazelcastConfigFile.exists()) ? hazelcastConfigFile.getAbsolutePath() : null;
        String hazelcastInstance = KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance");
        String hazelcastUser = KConfiguration.getInstance().getConfiguration().getString("hazelcast.user");

        // HAZELCAST SERVER ADDRESS
        String env = System.getenv("HAZELCAST_SERVER_ADDRESSES");
        List<String> envAddresses = env != null && !env.isEmpty()
                ? Arrays.asList(env.split(","))
                : new ArrayList<>();

        List<String> address = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("hazelcast.server.addresses", new ArrayList<>()), Functions.toStringFunction());

        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastClientConfigFile(hazelcastConfigFileS)
                .hazelcastInstance(hazelcastInstance)
                .setHazelcastServers( env != null && !env.isEmpty() ? envAddresses.toArray(new String[0]) : address.toArray(new String[0]))
                .hazelcastUser(hazelcastUser)
                .build();

        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost(solrProcessingHost)
                .objectStorePath(objectPath)
                .objectStorePattern(objectPattern)
                .datastreamStorePath(datastreamStorePath)
                .datastreamStorePattern(datastreamStorePattern)
                .hazelcastConfiguration(hazelcastConfig)
                .build();

        AkubraRepository akubraRepository = AkubraRepositoryFactory.createRepository(config);
        return akubraRepository;
    }

}
```
