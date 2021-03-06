/*******************************************************************************
 * Copyright (c) 2014 Karlsruhe Institute of Technology, Germany
 *                    Technical University Darmstadt, Germany
 *                    Chalmers University of Technology, Sweden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Technical University Darmstadt - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.key_project.sed.key.ui.test.testcase.swtbot;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.key_project.sed.core.model.ISEDDebugTarget;
import org.key_project.sed.core.model.ISEDLoopBodyTermination;
import org.key_project.sed.core.model.ISEDLoopInvariant;
import org.key_project.sed.core.model.ISEDMethodContract;
import org.key_project.sed.core.model.ISEDMethodReturn;
import org.key_project.sed.core.model.ISEDStatement;
import org.key_project.sed.core.model.ISEDTermination;
import org.key_project.sed.core.model.ISEDThread;
import org.key_project.sed.core.test.util.TestSedCoreUtil;
import org.key_project.sed.key.core.test.testcase.swtbot.AbstractKeYDebugTargetTestCase;
import org.key_project.sed.key.ui.test.Activator;
import org.key_project.util.test.util.SWTBotTabbedPropertyList;
import org.key_project.util.test.util.TestUtilsUtil;

/**
 * Provides the basic functionality to test KeY specific property tabs.
 * @author Martin Hentschel
 */
public class AbstractSWTBotKeYPropertyTabTest extends AbstractKeYDebugTargetTestCase {
   /**
    * Does some test steps on the all node types example.
    * @param projectName The project name to extract test data into.
    * @param steps The test steps to execute.
    * @throws Exception Occurred Exception.
    */
   protected void doAllNodeTypesTest(String projectName, final ITestSteps steps) throws Exception {
      IKeYDebugTargetProofFileTestExecutor executor = new IKeYDebugTargetProofFileTestExecutor() {
         @Override
         public void test(SWTWorkbenchBot bot, IJavaProject project, IFile file, String targetName, SWTBotView debugView, SWTBotTree debugTree, ISEDDebugTarget target, ILaunch launch) throws Exception {
            // Wait until jobs are done
            TestUtilsUtil.sleep(2000); // Give the UI the chance to start jobs
            TestUtilsUtil.waitForJobs(); // Wait until selection is synchronized
            // Get properties view
            SWTBotView propertiesView = TestUtilsUtil.getPropertiesView(bot);
            // Select first thread
            selectThread(debugTree);
            SWTBotTabbedPropertyList tabs = SWTBotTabbedPropertyList.tabbedPropertyList(propertiesView.bot());
            assertNotNull(tabs);
            steps.assertThread(debugTree, propertiesView, tabs);
            // Select first statement
            selectStatement(debugTree);
            steps.assertStatement(debugTree, propertiesView, tabs);
            // Select debug target
            selectDebugTarget(debugTree);
            steps.assertDebugTarget(debugTree, propertiesView, tabs);
            // Select method return
            selectMethodReturn(debugTree);
            steps.assertMethodReturn(debugTree, propertiesView, tabs);
            // Select method contract
            selectMethodContract(debugTree);
            steps.assertMethodContract(debugTree, propertiesView, tabs);
            // Select loop invariant
            selectLoopInvariant(debugTree);
            steps.assertLoopInvariant(debugTree, propertiesView, tabs);
            // Select loop body termination
            selectLoopBodyTermination(debugTree);
            steps.assertLoopBodyTermination(debugTree, propertiesView, tabs);
            // Select termination
            selectTermination(debugTree);
            steps.assertTermination(debugTree, propertiesView, tabs);
            // Select launch
            selectLaunch(debugTree);
            steps.assertLaunch(debugTree, propertiesView, tabs);
         }
      };
      doKeYDebugTargetTest(projectName, 
                           Activator.PLUGIN_ID, 
                           "data/allNodeTypesTest/test", 
                           false, 
                           false, 
                           new IFileSelector() {
                              @Override
                              public IFile getFile(IJavaProject project) throws Exception {
                                 return project.getProject().getFile(new Path("src/AllNodeTypesTest.proof"));
                              }
                           }, 
                           false, 
                           false, 
                           false, 
                           false, 
                           false, 
                           true,
                           8, 
                           executor);
   }

   /**
    * Selects an {@link ISEDMethodReturn}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectLaunch(SWTBotTree debugTree) {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0);
   }

   /**
    * Selects an {@link ISEDMethodReturn}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectMethodReturn(SWTBotTree debugTree) {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1, 0, 4, 1, 11);
   }

   /**
    * Selects an {@link ISEDMethodContract}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectMethodContract(SWTBotTree debugTree) {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1, 0, 0);
   }

   /**
    * Selects an {@link ISEDDebugTarget}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectDebugTarget(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0);
   }

   /**
    * Selects an {@link ISEDThread}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectThread(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0);
   }

   /**
    * Selects an {@link ISEDStatement}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectStatement(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1);
   }

   /**
    * Selects an {@link ISEDLoopInvariant}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectLoopInvariant(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1, 0, 4);
   }

   /**
    * Selects an {@link ISEDLoopBodyTermination}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectLoopBodyTermination(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1, 0, 4, 0, 2);
   }

   /**
    * Selects an {@link ISEDTermination}.
    * @param debugTree The {@link SWTBotTree} to select in.
    * @throws Exception Occurred Exception.
    */
   protected void selectTermination(SWTBotTree debugTree) throws Exception {
      TestSedCoreUtil.selectInDebugTree(debugTree, 0, 0, 0, 1, 0, 4, 1, 12);
   }

   /**
    * Defines the test steps to execute via {@link AbstractSWTBotKeYPropertyTabTest#doFlatStepsTest(ITestSteps)}.
    * @author Martin Hentschel
    */
   protected static interface ITestSteps {
      /**
       * Do some assertions on an {@link ISEDThread}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertThread(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDStatement}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertStatement(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDDebugTarget}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertDebugTarget(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDMethodReturn}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertMethodReturn(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDMethodContract}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertMethodContract(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDTermination}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertTermination(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDLoopInvariant}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertLoopInvariant(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ISEDLoopBodyTermination}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertLoopBodyTermination(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;

      /**
       * Do some assertions on an {@link ILaunch}.
       * @param debugTree The debug tree.
       * @param propertiesView The properties view.
       * @param tabs The properties view tabs.
       * @throws Exception Occurred Exception
       */
      public void assertLaunch(SWTBotTree debugTree, SWTBotView propertiesView, SWTBotTabbedPropertyList tabs) throws Exception;
   }
}