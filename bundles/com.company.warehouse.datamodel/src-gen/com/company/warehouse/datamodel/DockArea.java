/**
 */
package com.company.warehouse.datamodel;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dock Area</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.company.warehouse.datamodel.DockArea#getScenario <em>Scenario</em>}</li>
 *   <li>{@link com.company.warehouse.datamodel.DockArea#getId <em>Id</em>}</li>
 *   <li>{@link com.company.warehouse.datamodel.DockArea#getDirection <em>Direction</em>}</li>
 *   <li>{@link com.company.warehouse.datamodel.DockArea#getStoragePlaces <em>Storage Places</em>}</li>
 * </ul>
 *
 * @see com.company.warehouse.datamodel.DatamodelPackage#getDockArea()
 * @model
 * @generated
 */
public interface DockArea extends EObject {
	/**
	 * Returns the value of the '<em><b>Scenario</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link com.company.warehouse.datamodel.Scenario#getDockAreas <em>Dock Areas</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scenario</em>' container reference.
	 * @see #setScenario(Scenario)
	 * @see com.company.warehouse.datamodel.DatamodelPackage#getDockArea_Scenario()
	 * @see com.company.warehouse.datamodel.Scenario#getDockAreas
	 * @model opposite="dockAreas" transient="false"
	 * @generated
	 */
	Scenario getScenario();

	/**
	 * Sets the value of the '{@link com.company.warehouse.datamodel.DockArea#getScenario <em>Scenario</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scenario</em>' container reference.
	 * @see #getScenario()
	 * @generated
	 */
	void setScenario(Scenario value);

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see com.company.warehouse.datamodel.DatamodelPackage#getDockArea_Id()
	 * @model id="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link com.company.warehouse.datamodel.DockArea#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Direction</b></em>' attribute.
	 * The literals are from the enumeration {@link com.company.warehouse.datamodel.Direction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Direction</em>' attribute.
	 * @see com.company.warehouse.datamodel.Direction
	 * @see #setDirection(Direction)
	 * @see com.company.warehouse.datamodel.DatamodelPackage#getDockArea_Direction()
	 * @model
	 * @generated
	 */
	Direction getDirection();

	/**
	 * Sets the value of the '{@link com.company.warehouse.datamodel.DockArea#getDirection <em>Direction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Direction</em>' attribute.
	 * @see com.company.warehouse.datamodel.Direction
	 * @see #getDirection()
	 * @generated
	 */
	void setDirection(Direction value);

	/**
	 * Returns the value of the '<em><b>Storage Places</b></em>' reference list.
	 * The list contents are of type {@link com.company.warehouse.datamodel.Node}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Storage Places</em>' reference list.
	 * @see com.company.warehouse.datamodel.DatamodelPackage#getDockArea_StoragePlaces()
	 * @model
	 * @generated
	 */
	EList<Node> getStoragePlaces();

} // DockArea
