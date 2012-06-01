package biomine.bmvis2;


public class BMGraphAttributes {
	
	
    /**
     * Node and edge key for type (node type or linktype as String).
     */
    public static final String TYPE_KEY = "type";

    /**
     * Node key for the DBID (Database:ID as String).
     */
    public static final String DBID_KEY = "database_id";

    /**
     * Node key for the DB (Database as String).
     */
    public static final String DB_KEY = "database";

    /**
     * Node key for the ID (ID in Database as String).
     */
    public static final String ID_KEY = "id_in_db";

    /**
     * Node and edge key for position (in BMGraph, as String).
     */
    public static final String POS_KEY = "pos";


    
    /**
     * Node and edge(node) key for stored status of pinning (boolean).
     * Note that in the Visualization the pin status is the return value
     * of "isPositionFixed()" for nodes (real or edgenodes), and this
     * information is synced into the graphs only when positions are.
     */
    public static final String PINNED_KEY = "pinned";

    /**
     * Prefuse Node key for their "special" status (boolean). In the BMGraph
     * this is not an attribute of the nodes but rather the membership
     * in the graph-specific set of special nodes.
     */
    public static final String SPECIAL_KEY = "special";


    /**
     * Edge relevance key (double, read from graph).
     */
    public static final String RELEVANCE_KEY = "relevance";

    /**
     * Edge reliability key (double, read from graph).
     */
    public static final String RELIABILITY_KEY = "reliability";

    /**
     * Edge rarity key (double, read from graph).
     */
    public static final String RARITY_KEY = "rarity";

    /**
     * Edge goodness key (double, read from graph).
     */
    public static final String GOODNESS_KEY = "goodness";

    /**
     * Edge weight (non-discrete numeric value, double, read from graph).
     */
    public static final String WEIGHT_KEY = "weight";

    /**
     * Edge weight (of type flow) (non-discrete numeric value, double, read from graph).
     */
    public static final String FLOW_KEY = "flow";

    /**
     * Node or edge URL key (String, read from graph).
     */
    public static final String URL_KEY = "url";

    /**
     * Node or edge TTNR key (double, read from graph or generated).
     */
    public static final String TTNR_KEY = "ttnr";

    /**
     * Node or edge KTNR key (double, read from graph or generated).
     */
    public static final String KTNR_KEY = "ktnr";
    
    /**
     * Node or edge shortest paths key (boolean).
     */
    public static final String SHORTEST_KEY = "shortest";

    /**
     * Node or edge minimal paths key (boolean).
     */
    public static final String MINIMAL_KEY = "minimal";

    /**
     * Node or edge source database name key (String, read from graph).
     */
    public static final String SOURCE_DB_NAME_KEY = "source_db_name";

    /**
     * Node or edge source database version key (String, read from graph).
     */
    public static final String SOURCE_DB_VERSION_KEY = "source_db_version";

    /**
     * Node organism key (String, read from graph).
     */
    public static final String ORGANISM_KEY = "organism";

    /**
     * Node query set key (String).
     */
    public static final String QUERYSET_KEY = "queryset";

    /**
     * Item fill color key.
     *
     * @see biomine.bmvis2.color.DefaultNodeColoring for usage.
     */
    public static final String FILL_KEY = "fill";

    /**
     * Multiple colors key.
     * @see biomine.bmvis2.color.DefaultNodeColoring for usage.
     */

    public static final String FILLS_KEY = "fills";


}
