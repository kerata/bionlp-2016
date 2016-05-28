package BB3.Models;

import BB3.Utils.Commons;
import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * Created by Mert Tiftikci on 06/03/16.
 */
public class Tree {

    private Node root;
    private Map<String, ArrayList<Node>> nodes;
    private boolean hasMerged = false;

    public Tree() {
        nodes = new HashMap<>();
    }

    /**
     * Merges given tree into this tree from nodes in given parent node list.
     * @param parentPosting List of parent nodes within this tree
     * @param other Tree to be merged
     */
    public void merge(ArrayList<Node> parentPosting, Tree other) {
        parentPosting.replaceAll(attachFrom -> {
            Node newRoot = other.getRoot().generateCopy();
            newRoot.bringAllNodes(new ArrayList<>()).forEach(node -> {
                ArrayList<Node> ownedNodes = nodes.get(node.data.getId());
                if (ownedNodes != null)
                    ownedNodes.add(node);
                else {
                    ArrayList<Node> posting = new ArrayList<>();
                    posting.add(node);
                    nodes.put(node.data.getId(), posting);
                }
            });
            attachFrom.addChild(newRoot);
            return attachFrom;
        });
    }

    /**
     * Adds given node to tree by creating node postings for each node. If there exists a posting with
     * given node id, node is inserted to given posting. Same IDed nodes are automatically sorted by
     * their levels.
     *
     * @param node
     * @return
     */
    public ArrayList<Node> addToNodes(Node node) {
        ArrayList<Node> posting = nodes.get(node.data.getId());
        if (posting == null) {
            posting = new ArrayList<Node>() {
                public boolean add(Node mt) {
                    int index = Collections.binarySearch(this, mt);
                    if (index < 0) index = ~index;
                    super.add(index, mt);
                    return true;
                }
            };
            nodes.put(node.data.getId(), posting);
        }
        posting.add(node);
        return posting;
    }

    /**
     * Finds or creates {@link Node nodes} from given {@link Term terms} then creates parent child
     * relationship among them.
     * @param childData {@link Term Term} of the child node
     * @param parentData {@link Term Term} of the parent node
     * @return List of parent nodes
     */
    public ArrayList<Node> setParentNodeForData(@NotNull Term childData, @NotNull Term parentData) {
        ArrayList<Node> parentNodePosting = getNode(parentData, true);
        ArrayList<Node> childNodePosting = getNode(childData, true);

        for (Node parentNode: parentNodePosting)
            for (Node childNode: childNodePosting) {
                Node newNode = childNode.setParent(parentNode);
                if (newNode.level == 0)
                    root = newNode;
            }

        return parentNodePosting;
    }

    /**
     * <p>
     *     Sets root node for this tree, created from given {@link Term term} as data.
     *     Returns final root as a result. While constructing trees, if parent node found
     *     in one of trees, then merges current tree with it and tags this as merged.
     * </p>
     * <p>
     *     Iterates over every parent node to expand current tree, and merges whenever it
     *     finds a parent from previously created trees.
     * </p>
     *
     * @param ontology {@link Ontology Ontology} object that is under construction
     * @param terms {@link Term Terms} within given ontology file
     * @param rootData {@link Term Term} data to be constructed
     * @return root node
     */
    public Node constructForTerm(Ontology ontology, Map<String, Term> terms, Term rootData) {
        root = new Node(rootData);
        addToNodes(root);
        root.buildTree(this, ontology, terms);
        return root;
    }

    /**
     * Getter method, boolean that indicates if this tree has been merged into some other tree.
     * @return boolean
     */
    public boolean hasMerged() {
        return hasMerged;
    }

    /**
     * Getter method
     * @return root Node
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Fetches for {@link Node nodes} with given id and returns null when not found.
     * @param id of searched node
     * @return List of nodes with given id or null
     */
    public ArrayList<Node> getNode(String id) {
        return nodes.get(id);
    }

    /**
     * Fetches for {@link Node nodes} with given data and returns null when not found.
     * @param data of searched node
     * @return List of nodes containing given data or null
     */
    public ArrayList<Node> getNode(@NotNull Term data) {
        return getNode(data, false);
    }

    /**
     * Fetches for {@link Node nodes} with given data and returns null when not found.
     * If requested, creates a new Node from given data, adds and returns it to caller.
     *
     * @param data of searched node
     * @param shouldCreateIfNotContains Indicates if node should be created from given data
     * @return List of nodes containing given data
     */
    public ArrayList<Node> getNode(@NotNull Term data, boolean shouldCreateIfNotContains) {
        ArrayList<Node> result = getNode(data.getId());
        if (shouldCreateIfNotContains && result == null) {
            result = addToNodes(new Node(data));
        }
        return result;
    }

    /**
     * Finds first common roots of all nodes with given ids and prints it if requested.
     * @param ids of Nodes
     * @return List of root nodes
     */
    public ArrayList<Node> findCommonRoots(String[] ids, boolean shouldPrint) {
        ArrayList<Node> commonRoots = new ArrayList<>();
        // first two loop iterates over ids to find node groups to compare them all
        for (int i = 0;i < ids.length - 1;i++) {
            ArrayList<Node> lhs = getNode(ids[i]);
            for (int j = i + 1;j < ids.length;j++) {
                ArrayList<Node> rhs = getNode(ids[j]);

                // find common root for all nodes within found groups and add found roots
                // to resulting list
                lhs.forEach(mLhs ->
                    rhs.forEach(mRhs -> {
                        Node commonRoot = mLhs.findFirstCommonRoot(mRhs);
                        commonRoots.add(commonRoot);
                        // print if requested
                        if (shouldPrint)
                            Commons.printBlack(String.format("Roots of %s - %s: %s",
                                    mLhs.getData().getId(),
                                    mRhs.getData().getId(),
                                    commonRoot.getData().getId()));
                    }));
            }
        }
        return commonRoots;
    }

    /**
     * Prints tree, difference from String coming from toString method is:
     * this representation does not includes data but only ids.
     */
    public void printByLevel() {
        nodes.values().stream()
                .flatMap(Collection::stream)
                .max(Node::compareTo)
                .ifPresent(node ->
                        Commons.printBlack(root.toStringUntilLevel(node.level)));
    }

    /**
     * Prints tree with given depth limit.
     * @param maxLevel limit for print
     */
    public void printUntilLevel(int maxLevel) {
        Commons.printBlack(root.toStringUntilLevel(maxLevel));
    }

    /**
     * Builds string representation of this tree.
     *
     * See {@link Node#toString() toString} method of node.
     * @return String representation of this tree
     */
    @Override
    public String toString() {
        return root.toString();
    }

    public static class Node implements Comparable<Node> {

        int level = 0;
        Term data;
        Node parent;
        ArrayList<Node> children;

        /**
         * Constructor for Node using {@link Term term} as data.
         * @param data
         */
        public Node(Term data) {
            this.data = data;
            this.children = new ArrayList<>();
            parent = this;
        }

        /**
         * Copy constructor for Node
         * @param copy Node to be copied
         */
        public Node(Node copy) {
            this.data = copy.data;
            this.children = copy.children;
            this.parent = copy.parent;
        }

        /**
         * Getter method
         * @return level of this term in tree
         */
        public int getLevel() {
            return level;
        }

        /**
         * Getter method
         * @return data in node as Term
         */
        public Term getData() {
            return data;
        }

        /**
         * Getter method
         * @return Parent node
         */
        public Node getParent() {
            return parent;
        }

        /**
         * Getter method.
         * @return Children {@link Node node}
         */
        public ArrayList<Node> getChildren() {
            return children;
        }

        /**
         * Creates a {@link Node node} from given {@link Term term} as data and sets it as
         * parent.
         * {@link #addChild(Node) setParent} method
         *
         * @param parentData Parent node data as Term
         * @return Parent node
         */
        public Node setParent(Term parentData) {
            return setParent(new Node(parentData));
        }

        /**
         * Sets parent of this {@link Node node} and add this as child to that.
         *
         * @param parentNode Parent node
         * @return Parent node
         */
        public Node setParent(Node parentNode) {
            parent = parentNode;
            parent.addChild(this);
            return parent;
        }

        /**
         * Creates a {@link Node node} from given {@link Term term} as data and adds it as
         * child.
         * {@link #addChild(Node) addChildForData} method
         *
         * @param childData Child node data as Term
         * @return Child node
         */
        public Node addChild(Term childData) {
            return addChild(new Node(childData));
        }

        /**
         * Adds given {@link Node node} as child to this node, sets its level and parent
         * as this node as well.
         * @param childNode Child node
         * @return Child node
         */
        public Node addChild(Node childNode) {
            childNode.setLevel(level + 1);
            childNode.parent = this;
            children.add(childNode);
            return childNode;
        }

        /**
         * Sets level of current {@link Node node}, adjusts levels of children
         * {@link Node nodes} accordingly as well.
         *
         * @param level Of current node
         */
        public void setLevel(int level) {
            this.level = level;
            children.forEach(child -> {
                if (child != null)
                    child.setLevel(level + 1);
            });
        }

        /**
         * Generates a copy of this node, containing all children nodes as well.
         * @return Copy of current Node
         */
        public Node generateCopy() {
            Node copyNode = new Node(data);
            for (Node child: children)
                child.generateCopy().setParent(copyNode);
            return copyNode;
        }

        /**
         * Accumulates current {@link Node node} all children {@link Node nodes}.
         *
         * @param nodes Container to add this all children nodes
         * @return Container includes all nodes
         */
        public ArrayList<Node> bringAllNodes(ArrayList<Node> nodes) {
            nodes.add(this);
            for (Node child: children)
                child.bringAllNodes(nodes);
            return nodes;
        }

        /**
         * <p>
         *     Tries to build sub trees from given {@link Term term} bottom-up style since
         *     relations "is_a" points to parents. Incorporates created trees to accumulated
         *     trees that has been created from terms in ontology if there is a relation.
         * </p>
         * <p>
         *     There could be more than one parent for each term. Created tree is duplicated
         *     before merging it to the parent tree in these cases so that each group would
         *     have only one parent and tree structure is preserved.
         * </p>
         *
         * @param holder {@link Tree Tree} object contains this node
         * @param ontology {@link Ontology Ontology} object that is under construction
         * @param terms {@link Term Terms} within given ontology file
         */
        public void buildTree(Tree holder, Ontology ontology, Map<String, Term> terms) {
            data.getIs_a().forEach(relation -> {
                Term parentTerm = terms.get(relation.getTermId());
                if (parentTerm == null) {
                    ontology.getDependencyTrees().forEach(tree -> {
                        ArrayList<Node> parentPosting = tree.getNode(relation.getTermId());
                        if (parentPosting != null) {
                            tree.merge(parentPosting, holder);
                            holder.hasMerged = true;
                        }
                    });
                }
                else {
                    ArrayList<Node> parentPosting = holder.setParentNodeForData(this.data, parentTerm);
                    terms.remove(parentTerm.getId());

                    for (Node node: parentPosting)
                        if (node.data.equals(parentTerm)) {
                            node.buildTree(holder, ontology, terms);
                            break;
                        }
                }
            });
        }

        /**
         * Tries to find first common root with given {@link Node node}.
         *
         * @param other Node to be processed
         * @return Common root node if exists, if not; one that is in lower level
         */
        public Node findFirstCommonRoot(Node other) {
            // If they are same
            if (this.equals(other))
                return this;
            else {
                Node lhs = this.level > other.level ? this : other;
                Node rhs = this.level > other.level ? other : this;
                while (lhs.level != rhs.level)
                    lhs = lhs.parent;

                while (!lhs.equals(rhs)) {
                    lhs = lhs.parent;
                    rhs = rhs.parent;
                }

                return lhs;
            }
        }

        /**
         * Builds tree representation of this node and its children
         * until provided maxLevel child.
         *
         * @param maxLevel Build until this level
         * @return String in tree form for this node and its children until maxLevel
         */
        public String toStringUntilLevel(int maxLevel) {
            if (maxLevel < 0) return null;

            StringBuilder accumulator = new StringBuilder();
            // every bar denotes level of the node
            for (int i = 0;i < level;i++)
                accumulator.append("|");
            // id of the node
            accumulator.append(data.getId());

            // if level is smaller than maxLevel, continue with its children
            // or indicate its the leaf node.
            if (level < maxLevel) {
                if (children.size() == 0)
                    accumulator
                            .append("\n");
                else {
                    accumulator
                            .append("-")
                            .append("\n");
                    for (Node child: children)
                        accumulator.append(child.toStringUntilLevel(maxLevel));
                }
            }
            // if level is at max, do not include children
            else if (level == maxLevel) {
                if (children.size() > 0)
                    accumulator.append("+");
                accumulator.append("\n");
            }
            return accumulator.toString();
        }

        /**
         * Comparator interface method, compares nodes according to their level.
         *
         * @param node Node to be compared
         * @return Comparison result in int form
         */
        @Override
        public int compareTo(Node node) {
            int diff = level - node.level;
            return diff < 0 ? -1 :
                    diff == 0 ? 0 : 1;
        }

        /**
         * Compares this node with given object; if it is a {@link Node node} object with same id,
         * result will be true otherwise it is false
         *
         * @param obj Object to be compared with
         * @return Boolean for equivalence
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node other = (Node) obj;
                return other.data.getId().equals(data.getId());
            }
            else return false;
        }

        /**
         * Builds tree representation of this and its children. If called on the root node, it will generate full tree.
         * @return String in tree form for this node and its children
         */
        @Override
        public String toString() {
            StringBuilder accumulator = new StringBuilder();
            for (int i = 0;i < level;i++)
                accumulator.append(" ");
            accumulator
                    .append(level)
                    .append(data.toString())
                    .append("\n");

            for (Node child: children)
                accumulator.append(child.toString());
            return accumulator.toString();
        }
    }
}