package service.auxiliary;

import java.util.ArrayList;
import java.util.List;

public class StaticTree<C> {

	private StaticTreeNode<C> startNode;
	private List<StaticTreeNode<C>> allNodes = new ArrayList<>();
	
	public StaticTree() {
		startNode = new StaticTreeNode<C>(null, null);
	}
	
	public void addNodes(List<C> contents) {
		
		StaticTreeNode<C> currentNode = startNode;
		
		for (C content : contents) {
			
			if (!currentNode.hasChildWithContent(content)) {
				currentNode = new StaticTreeNode<C>(content, currentNode);
				allNodes.add(currentNode);
			}
			else {
				currentNode = currentNode.findChildWithContent(content);
			}
		}
		
	}
	
	public List<C> findNodePath(C content) throws IllegalArgumentException {
		
		StaticTreeNode<C> contentNode = findNodeWithContent(content);
		
		if (contentNode == null) {
			throw new IllegalArgumentException("The given content is not available in the tree!");
		}
		
		List<C> contentList = new ArrayList<>();
		
		while (contentNode != null && !contentNode.equals(startNode)) {
			contentList.add(contentNode.getContent());
			contentNode = contentNode.getParent();
		}
		
		return contentList;
	}
	
	public int getTreeSize() {
		return allNodes.size();
	}
	
	private StaticTreeNode<C> findNodeWithContent(C content) {
		
		StaticTreeNode<C> currentNode = startNode.getNext();
		
		while (currentNode != null) {
			if (!currentNode.equals(startNode) && currentNode.getContent().equals(content)) {
				resetVisited();
				return currentNode;
			}
			
			currentNode = currentNode.getNext();
		}
		
		resetVisited();
		return null;
	}
	
	private void resetVisited() {
		for (StaticTreeNode<C> node : allNodes) {
			node.reset();
		}
	}
}
