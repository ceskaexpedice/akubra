/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra.access;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is main point to access to fedora through REST-API
 *
 * @author pavels
 * 
 */
public interface RepositoryAccess {

    // object
    boolean objectExists(String pid);

    RepositoryObjectWrapper getObject(String pid, FoxmlType foxmlType);

    RepositoryObjectProperties getObjectProperties();

    //void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    //void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

    // datastream
    boolean datastreamExists(String pid, String dsId);

    //- getMimeType , getCreatedData, (typ x,M,....control-group)
    DatastreamMetadata getDatastreamMetadata(String pid, String dsId);

    RepositoryObjectWrapper getDatastreamContent(String pid, String dsId);

    RepositoryObjectWrapper getDatastreamContent(String pid, String dsId, String version);

    RelsExtWrapper processDatastreamRelsExt(String pid);

    List<String> getDatastreamNames(String pid);


    // Processing index
    void queryProcessingIndex(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> mapper);

    //------------- podpora zamku zvlast

    /*
    void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri);

    void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds);

    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray);

    public void deleteDatastream(String pid, KnownDatastreams dsId);


    void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

*/

    //------------------------------------------------------------
    //public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException;


    /**
     * Check if the object is available
     * @param pid Pid of object 
     * @return true or false  - object objectExists or doesn't exist
     * @throws IOException
     */
    //public boolean isObjectAvailable(String pid) throws IOException;
    
    /**
     * Checks whether content is acessiable
     *
     * @param pid Tested object
     * @return true if object is accessible
     * @throws IOException IO error has been occurred
     */
    /*
    public boolean isContentAccessible(String pid) throws IOException;


    public Repository getInternalAPI() throws RepositoryException;

    public Repository getTransactionAwareInternalAPI() throws RepositoryException;
*/

    /**
     * Collects and returns subtree as one set
     *
     * @param pid Root pid
     * @return all subtree as set
     * @throws IOException IO error has been occurred
     */
//    public List<String> getPids(String pid) throws IOException;

    


    /**
     * Returns current version of fedora
     *
     * @return version
     * @throws IOException Cannot detect current version
     */
   // public String getFedoraVersion() throws IOException;


    /**
     * TODO: Not used
     * Datastreams description document
     *
     * @param pid PID of requested object
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    /*
    Document getObjectProfile(String pid) throws IOException;


    Date getObjectLastmodifiedFlag(String pid) throws IOException;

    @Deprecated
    List<Map<String, String>> getStreamsOfObject(String pid)  throws IOException;

    InputStream getFoxml(String pid, boolean archive) throws IOException;

    default void shutdown(){};

     */


//    private Date lastModified(String pid, String stream) throws IOException {
//        Date date = null;
//        Document streamProfile = fedoraAccess.getStreamProfile(pid, stream);
//
//        Element elm = XMLUtils.findElement(streamProfile.getDocumentElement(),
//                "dsCreateDate",
//                FedoraNamespaces.FEDORA_MANAGEMENT_NAMESPACE_URI);
//        if (elm != null) {
//            String textContent = elm.getTextContent();
//            for (DateFormat df : XSD_DATE_FORMATS) {
//                try {
//                    date = df.parse(textContent);
//                    break;
//                } catch (ParseException e) {
//                    //
//                }
//            }
//        }
//        if (date == null) {
//            date = new Date();
//        }
//        return date;
//    }

    //----------------------------------------------------------------

    //public static final String NAMESPACE_FOXML = "info:fedora/fedora-system:def/foxml#";

    /**
     * @se RepositoryApiTimestampFormatterTest
     */
    /*
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

     */

    //TODO: methods for fetching other types of datastreams (redirect, external referenced, probably not managed)
    //TODO: methods for updating datastreams (new versions)

    //CREATE

    //READ

   //public String getProperty(String pid, String propertyName) throws IOException, RepositoryException;








    //DELETE

    /*
    class Triplet {
        public final String source;
        public final String relation;
        public final String target;

        public Triplet(String source, String relation, String target) {
            this.source = source;
            this.relation = relation;
            this.target = target;
        }

        @Override
        public String toString() {
            return String.format("%s -%s-> %s", source, relation, target);
        }
    }*/
/*
    class TitlePidPairs {
        public List<Pair<String, String>> titlePidPairs;
        public String nextCursorMark;
    }*/

    //---------------KRRepAPI

    /*
    public static class KnownXmlFormatUris {
        public static final String RELS_EXT = "info:fedora/fedora-system:FedoraRELSExt-1.0";
        public static final String BIBLIO_MODS = "http://www.loc.gov/mods/v3";
        public static final String BIBLIO_DC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    }*/





    /*
    public enum OwnRelationsMapping {

        page{
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_PAGE;
            }
        },

        unit {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_UNIT;
            }
        },
        periodicalvolume {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_VOLUME;
            }
        },
        volume {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_VOLUME;
            }
        },
        periodicalitem {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_ITEM;
            }
        },
        supplement {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_ITEM;
            }
        },
        soundunit {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT;
                //return KnownRelations.CONTAINS_TRACK;
            }
        },
        soundrecording {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT;
            }
        },

        internalpart {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART;
            }
        },
        track {

            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.CONTAINS_TRACK;
            }

        },
        article {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART;
            }
        };

        ;

        public static KrameriusRepositoryApi.OwnRelationsMapping find(String name) {
            KrameriusRepositoryApi.OwnRelationsMapping[] values = values();
            for (KrameriusRepositoryApi.OwnRelationsMapping relMap :  values()) {
                if (relMap.name().equals(name)) {
                    return relMap;
                }
            }
            return null;
        }

        public abstract KrameriusRepositoryApi.KnownRelations relation();

    }
*/

    /*
    public enum FosterRelationsMapping {
        page{
            @Override
            public KrameriusRepositoryApi.KnownRelations relation(String parentModel) {
                List<String> parent = Arrays.asList("article", "internalpart");
                if (parent.contains(parentModel)) {
                    return KrameriusRepositoryApi.KnownRelations.IS_ON_PAGE;
                } else return KrameriusRepositoryApi.KnownRelations.CONTAINS;
            }
        },
        anything {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation(String parentModel) {
                return KrameriusRepositoryApi.KnownRelations.CONTAINS;
            }
        };

        public static KrameriusRepositoryApi.FosterRelationsMapping find(String name) {
            KrameriusRepositoryApi.FosterRelationsMapping[] values = KrameriusRepositoryApi.FosterRelationsMapping.values();
            for (KrameriusRepositoryApi.FosterRelationsMapping relMap : values) {
                if (relMap.name().equals(name)) return relMap;
            }
            return anything;

        }

        public abstract KrameriusRepositoryApi.KnownRelations relation(String parentModel);

    }
*/

    /*
    List<KrameriusRepositoryApi.KnownRelations> OWN_RELATIONS = Arrays.asList(new KrameriusRepositoryApi.KnownRelations[]{
            KrameriusRepositoryApi.KnownRelations.HAS_PAGE, KrameriusRepositoryApi.KnownRelations.HAS_UNIT, KrameriusRepositoryApi.KnownRelations.HAS_VOLUME, KrameriusRepositoryApi.KnownRelations.HAS_ITEM,
            KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT, KrameriusRepositoryApi.KnownRelations.HAS_TRACK, KrameriusRepositoryApi.KnownRelations.CONTAINS_TRACK, KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART
    });
    List<KrameriusRepositoryApi.KnownRelations> FOSTER_RELATIONS = Arrays.asList(new KrameriusRepositoryApi.KnownRelations[]{
            KrameriusRepositoryApi.KnownRelations.IS_ON_PAGE, KrameriusRepositoryApi.KnownRelations.CONTAINS
    });

     */

    /*
    static boolean isOwnRelation(String relation) {
        for (KrameriusRepositoryApi.KnownRelations knownRelation : OWN_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return true;
            }
        }
        for (KrameriusRepositoryApi.KnownRelations knownRelation : FOSTER_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return false;
            }
        }
        throw new IllegalArgumentException(String.format("unknown relation '%s'", relation));
    }

     */

    //TODO: methods for updating datastream data (done for inline xml datastreams)

    /**
     * @return Low level repository API. Through that can be accessed any kind of datastream or property, regardless if it is used by Kramerius or not
     */
   // public RepositoryApi getLowLevelApi();





}
