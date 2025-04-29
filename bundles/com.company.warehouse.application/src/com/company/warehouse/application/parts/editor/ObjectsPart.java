package com.company.warehouse.application.parts.editor;

import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;

import org.eclipse.emf.common.command.Command;
import com.amalgamasimulation.emf.commands.RemoveCommand;
import com.company.warehouse.datamodel.data.EMFExcelDataTransform;
import com.company.warehouse.application.utils.TreeElementType;
import com.company.warehouse.application.utils.AppData;
import com.company.warehouse.application.utils.Messages;
import com.amalgamasimulation.desktop.ui.editor.parts.AbstractObjectsPart;
import com.amalgamasimulation.desktop.ui.editor.parts.ObjectsPage;
import com.amalgamasimulation.desktop.ui.editor.utils.Topics;
import com.amalgamasimulation.desktop.ui.tables.EMFTable;
import com.amalgamasimulation.emf.commands.CommandsManager;
import com.amalgamasimulation.utils.format.Formats;
import com.company.warehouse.datamodel.Agent;
import com.company.warehouse.datamodel.DatamodelFactory;
import com.company.warehouse.datamodel.Arc;
import com.company.warehouse.datamodel.DatamodelPackage;
import com.company.warehouse.datamodel.Node;
import com.company.warehouse.datamodel.Scenario;
import com.amalgamasimulation.desktop.binding.UpdateValueStrategyFactory;

public class ObjectsPart extends AbstractObjectsPart{
	@Inject
	private AppData appData;
	
	@Inject
	@Translation
	private Messages messages;
	
	@SuppressWarnings("all")
	protected IObservableValue<Scenario> scenarioObservable = new WritableValue<>();
	@SuppressWarnings("all")
	private IObservableList<Node> nodeListObservable = EMFProperties.list(DatamodelPackage.Literals.SCENARIO__NODES).observeDetail(scenarioObservable);
	@SuppressWarnings("all")
	private IObservableList<Arc> arcListObservable = EMFProperties.list(DatamodelPackage.Literals.SCENARIO__ARCS).observeDetail(scenarioObservable);
	@SuppressWarnings("all")
	private IObservableList<Agent> agentListObservable = EMFProperties.list(DatamodelPackage.Literals.SCENARIO__FORKLIFTS).observeDetail(scenarioObservable);
	
		@Override
	protected List<ObjectsPage<? extends EObject>> getObjectsPages() {
		eventBroker.subscribe(Topics.NEW_SCENARIO, event -> scenarioObservable.setValue(appData.getScenario()));		
		List<ObjectsPage<? extends EObject>> pages = new ArrayList<ObjectsPage<? extends EObject>>();
		pages.add(createNodePage());
		pages.add(createArcPage());
		pages.add(createAgentPage());
		return pages;
	}
	
	private ObjectsPage<Node> createNodePage() {
		return new ObjectsPage<Node>(nodeListObservable, TreeElementType.NODE)
				.setTableRefreshBinding(
			DatamodelPackage.Literals.NODE__X,
					DatamodelPackage.Literals.NODE__ID,
					DatamodelPackage.Literals.NODE__NAME,
			DatamodelPackage.Literals.NODE__Y
					)
				.setAfterCreateTableElementAction(tableView -> {
					tableView	.column(Node::getId)
								.name(messages.obj_NODE_col_ID)
								.width(120)
								.emfTextIdEditor()
									.elements(nodeListObservable)
									.idFeature(DatamodelPackage.Literals.NODE__ID)
									.build();
					tableView	.column(p -> p == null ? "" : p.getName())
								.name(messages.obj_NODE_col_NAME)
								.width(100)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.NODE__NAME)
									.strategy(UpdateValueStrategyFactory.stringIsNotEmpty())
									.build();



					tableView	.column(Node::getX)
								.name(messages.obj_NODE_col_X)
								.width(100)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.NODE__X)
									.strategy(UpdateValueStrategyFactory.doubleAny())
									.build();
					tableView	.column(Node::getY)
								.name(messages.obj_NODE_col_Y)
								.width(100)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.NODE__Y)
									.strategy(UpdateValueStrategyFactory.doubleAny())
									.build();

					tableView.getEditingInExcelManager().setDataForEditing(
							DatamodelPackage.Literals.NODE,
							() -> scenarioObservable.getValue(),
							EMFExcelDataTransform.getExcelTransform(),
							() -> commandFactory.getCrossReferenceAdapter());
					tableView.getEditingInExcelManager().setAdditionalActionBeforeRemove(this::beforeRemoveNodes);
				});
	}

	private List<Command> beforeRemoveNodes(List<EObject> objects) {
		Set<Arc> removeArcs = new HashSet<>();
		Map<Node, Set<Arc>> nodeByArcs = new HashMap<>();
		List<Command> commands = new ArrayList<>();
		
		arcListObservable.forEach(arc -> {
			if (arc.getSource() != null) {
				nodeByArcs.computeIfAbsent(arc.getSource(), v -> new HashSet<>()).add(arc);
			}
			if (arc.getDest() != null) {
				nodeByArcs.computeIfAbsent(arc.getDest(), v -> new HashSet<>()).add(arc);
			}
		});
		
		objects.forEach(node -> removeArcs.addAll(nodeByArcs.get(node)));
		if (!removeArcs.isEmpty()) {
			commands.add(new RemoveCommand<Arc>(new ArrayList<>(removeArcs), commandFactory.getCrossReferenceAdapter()));
		}
		return commands;
	}


	
	private ObjectsPage<Arc> createArcPage() {
		return new ObjectsPage<Arc>(arcListObservable, TreeElementType.ARC)
				.setTableRefreshBinding(DatamodelPackage.Literals.ARC__SOURCE, DatamodelPackage.Literals.ARC__ID,DatamodelPackage.Literals.ARC__NAME,
					DatamodelPackage.Literals.ARC__DEST)
				.setAfterCreateTableElementAction(tableView -> {
					tableView	.column(Arc::getId)
								.name(messages.obj_ARC_col_ID)
								.width(100)
								.emfTextIdEditor()
									.elements(arcListObservable)
									.idFeature(DatamodelPackage.Literals.ARC__ID)
									.build();

					tableView	.column(p -> p == null ? "" : p.getName())
								.name(messages.obj_ARC_col_NAME)
								.width(100)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.ARC__NAME)
									.strategy(UpdateValueStrategyFactory.stringIsNotEmpty())
									.build();
					tableView	.column(Arc::getSource)
								.format(p -> p == null ? "" : p.getId())
								.name(messages.obj_ARC_col_SOURCE)
								.width(100)
								.emfAutoCompleteComboEditor()
									.elements(nodeListObservable)
									.feature(DatamodelPackage.Literals.ARC__SOURCE)
									.nameFeature(DatamodelPackage.Literals.NODE__ID)
									.build();
					tableView	.column(Arc::getDest)
								.format(p -> p == null ? "" : p.getId())
								.name(messages.obj_ARC_col_DEST)
								.width(100)
								.emfAutoCompleteComboEditor()
									.elements(nodeListObservable)
									.feature(DatamodelPackage.Literals.ARC__DEST)
									.nameFeature(DatamodelPackage.Literals.NODE__ID)
									.build();

					tableView.getEditingInExcelManager().setDataForEditing(DatamodelPackage.Literals.ARC,
							() -> scenarioObservable.getValue(), EMFExcelDataTransform.getExcelTransform(), () -> commandFactory.getCrossReferenceAdapter());
				});
	}
	
	private ObjectsPage<Agent> createAgentPage() {
		return new ObjectsPage<Agent>(agentListObservable, TreeElementType.AGENT, 
				() -> commandFactory.create(appData.getScenario(), messages.column_agent, () -> {
					Agent agent = DatamodelFactory.eINSTANCE.createAgent();
					if(!scenarioObservable.getValue().getNodes().isEmpty()) {
						agent.setBasePosition(scenarioObservable.getValue().getNodes().get(0));
					}
					return agent;
				}, true).getObject(),
				elem -> commandFactory.remove(elem, true),
				elem -> commandFactory.copy(elem, -1, true, true).getObject()) {
			@Override
			protected void updateChecked(EMFTable<Agent> tableView) {
				tableView.checkItems(agentListObservable.stream().filter(agent -> agent.isIncluded()).toList());
			}
		}
				.setCheckbox(true)
				.setTableRefreshBinding(
						DatamodelPackage.Literals.AGENT__ID, 
						DatamodelPackage.Literals.AGENT__NAME,
						DatamodelPackage.Literals.AGENT__BASE_POSITION,
						DatamodelPackage.Literals.AGENT__INCLUDED,
						DatamodelPackage.Literals.AGENT__VELOCITY)
				.setAfterCreateTableElementAction(tableView -> {
					tableView	.column(Agent::getId)
								.name(messages.column_agent_id)
								.width(100)
								.emfTextIdEditor()
									.elements(agentListObservable)
									.idFeature(DatamodelPackage.Literals.AGENT__ID)
									.build();
					tableView	.column(p -> p == null ? "" : p.getName())
								.name(messages.column_agent_name)
								.width(100)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.AGENT__NAME)
									.strategy(UpdateValueStrategyFactory.stringIsNotEmpty())
									.build();
					tableView	.column(Agent::getBasePosition)
								.name(messages.column_agent_base_position)
								.width(100)
								.format(p -> p == null ? "" : p.getId())
								.emfAutoCompleteComboEditor()
									.elements(nodeListObservable)
									.feature(DatamodelPackage.Literals.AGENT__BASE_POSITION)
									.nameFeature(DatamodelPackage.Literals.NODE__ID)
									.build();
					tableView	.column(Agent::getVelocity)
								.name(messages.column_agent_velocity)
								.width(100)
								.format(Formats.getDefaultFormats()::twoDecimals)
								.emfTextEditor()
									.feature(DatamodelPackage.Literals.AGENT__VELOCITY)
									.strategy(UpdateValueStrategyFactory.doubleAny())
									.build();


					
					tableView.addCheckedItemsChangedHandler(list -> {
						List<Agent> equipments = new ArrayList<>(scenarioObservable.getValue().getForklifts());
						equipments.removeAll(list);
						
						CompoundCommand com = new CompoundCommand();
						equipments.forEach(ew -> com.append(SetCommand.create(CommandsManager.getEditingDomain(), ew, DatamodelPackage.Literals.AGENT__INCLUDED, false)));
						list.forEach(ew -> com.append(SetCommand.create(CommandsManager.getEditingDomain(), ew, DatamodelPackage.Literals.AGENT__INCLUDED, true)));
						
						if(!com.isEmpty()) {
							CommandsManager.getEditingDomain().getCommandStack().execute(com);
						}
					});
					tableView.getEditingInExcelManager().setDataForEditing(DatamodelPackage.Literals.AGENT,
							() -> scenarioObservable.getValue(), EMFExcelDataTransform.getExcelTransform(), () -> commandFactory.getCrossReferenceAdapter());
					
				});
	}
}

