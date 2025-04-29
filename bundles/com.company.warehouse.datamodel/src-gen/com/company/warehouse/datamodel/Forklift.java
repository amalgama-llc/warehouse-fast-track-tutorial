/**
 */
package com.company.warehouse.datamodel;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Forklift</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.company.warehouse.datamodel.Forklift#getScenario <em>Scenario</em>}</li>
 * </ul>
 *
 * @see com.company.warehouse.datamodel.DatamodelPackage#getForklift()
 * @model
 * @generated
 */
public interface Forklift extends Agent {
	/**
	 * Returns the value of the '<em><b>Scenario</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link com.company.warehouse.datamodel.Scenario#getForklifts <em>Forklifts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scenario</em>' container reference.
	 * @see #setScenario(Scenario)
	 * @see com.company.warehouse.datamodel.DatamodelPackage#getForklift_Scenario()
	 * @see com.company.warehouse.datamodel.Scenario#getForklifts
	 * @model opposite="forklifts" transient="false"
	 * @generated
	 */
	Scenario getScenario();

	/**
	 * Sets the value of the '{@link com.company.warehouse.datamodel.Forklift#getScenario <em>Scenario</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scenario</em>' container reference.
	 * @see #getScenario()
	 * @generated
	 */
	void setScenario(Scenario value);

} // Forklift
