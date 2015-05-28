package completely.text.index;

import completely.text.match.Automaton;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static completely.common.Precondition.checkPointer;

/**
 * Trie based implementation of the {@link FuzzyIndex} interface.
 *
 * <p>Note that this implementation is not synchronized.
 */
public class Trie<V> extends AbstractIndex<V> implements FuzzyIndex<V>
{
    private Node root;

    public Trie()
    {
        root = new Node();
    }

    @Override
    public void clear()
    {
        root = new Node();
    }

    @Override
    public Set<V> getAll(String key)
    {
        checkPointer(key != null);
        Node node = find(root, key);
        if (node != null)
        {
            return new HashSet<V>(node.values);
        }
        return new HashSet<V>();
    }

    @Override
    public Set<V> getAny(String fragment)
    {
        checkPointer(fragment != null);
        Node node = find(root, fragment);
        if (node != null)
        {
            return values(node);
        }
        return new HashSet<V>();
    }

    @Override
    public Set<V> getAny(Automaton matcher)
    {
        checkPointer(matcher != null);
        Set<V> result = new HashSet<V>();
        for (Node node : findAll(root, matcher))
        {
            result.addAll(values(node));
        }
        return result;
    }

    @Override
    public boolean isEmpty()
    {
        return root.isEmpty();
    }

    @Override
    public boolean putAll(String key, Collection<V> values)
    {
        checkPointer(key != null);
        checkPointer(values != null);
        return putAll(root, key, values);
    }

    @Override
    public boolean removeAll(Collection<V> values)
    {
        checkPointer(values != null);
        return removeAll(root, values);
    }

    @Override
    public Set<V> removeAll(String key)
    {
        checkPointer(key != null);
        return removeAll(root, key);
    }

    @Override
    public boolean removeAll(String key, Collection<V> values)
    {
        checkPointer(key != null);
        checkPointer(values != null);
        return removeAll(root, key, values);
    }

    @Override
    public int size()
    {
        return size(root);
    }

    private Node find(Node node, String key)
    {
        assert node != null;
        assert key != null;
        if (key.length() <= 0)
        {
            return node;
        }
        else
        {
            char edge = key.charAt(0);
            Node child = node.children.get(edge);
            if (child != null)
            {
                return find(child, key.substring(1));
            }
        }
        return null;
    }

    private Collection<Node> findAll(Node node, Automaton matcher)
    {
        assert node != null;
        assert matcher != null;
        if (matcher.isWordAccepted())
        {
            return Arrays.asList(node);
        }
        else if (!matcher.isWordRejected())
        {
            List<Node> result = new LinkedList<Node>();
            for (Entry<Character, Node> child : node.children.entrySet())
            {
                char edge = child.getKey();
                result.addAll(findAll(child.getValue(), matcher.step(edge)));
            }
            return result;
        }
        return Collections.<Node>emptyList();
    }

    private boolean putAll(Node node, String key, Collection<V> values)
    {
        assert node != null;
        assert key != null;
        assert values != null;
        if (key.length() <= 0)
        {
            return node.values.addAll(values);
        }
        else
        {
            char edge = key.charAt(0);
            Node child = node.children.get(edge);
            if (child == null)
            {
                child = new Node();
                node.children.put(edge, child);
            }
            return putAll(child, key.substring(1), values);
        }
    }

    private boolean removeAll(Node node, Collection<V> values)
    {
        assert node != null;
        assert values != null;
        boolean result = node.values.removeAll(values);
        for (Iterator<Node> iterator = node.children.values().iterator(); iterator.hasNext();)
        {
            Node child = iterator.next();
            if (removeAll(child, values))
            {
                result = true;
            }
            if (child.isEmpty())
            {
                iterator.remove();
            }
        }
        return result;
    }

    private Set<V> removeAll(Node node, String key)
    {
        assert node != null;
        assert key != null;
        if (key.length() <= 0)
        {
            Set<V> result = node.values;
            node.values = new HashSet<V>();
            return result;
        }
        else
        {
            char edge = key.charAt(0);
            Node child = node.children.get(edge);
            if (child != null)
            {
                Set<V> result = removeAll(child, key.substring(1));
                if (child.isEmpty())
                {
                    node.children.remove(edge);
                }
                return result;
            }
        }
        return Collections.<V>emptySet();
    }

    private boolean removeAll(Node node, String key, Collection<V> values)
    {
        assert node != null;
        assert key != null;
        assert values != null;
        if (key.length() <= 0)
        {
            return node.values.removeAll(values);
        }
        else
        {
            char edge = key.charAt(0);
            Node child = node.children.get(edge);
            if (child != null)
            {
                boolean result = removeAll(child, key.substring(1), values);
                if (child.isEmpty())
                {
                    node.children.remove(edge);
                }
                return result;
            }
        }
        return false;
    }

    private int size(Node node)
    {
        assert node != null;
        int result = node.values.size();
        for (Node child : node.children.values())
        {
            result += size(child);
        }
        return result;
    }

    private Set<V> values(Node node)
    {
        assert node != null;
        Set<V> result = new HashSet<V>(node.values);
        for (Node child : node.children.values())
        {
            result.addAll(values(child));
        }
        return result;
    }

    private class Node
    {
        private Map<Character, Node> children;
        private Set<V> values;

        Node()
        {
            children = new HashMap<Character, Node>();
            values = new HashSet<V>();
        }

        boolean isEmpty()
        {
            return children.isEmpty() && values.isEmpty();
        }
    }
}
