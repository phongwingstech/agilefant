package fi.hut.soberit.agilefant.web;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork.Action;

import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.IterationDAO;
import fi.hut.soberit.agilefant.db.ProductDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Priority;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.security.SecurityUtil;
import fi.hut.soberit.agilefant.util.SpringTestCase;
import fi.hut.soberit.agilefant.util.TestUtility;

public class IterationActionTest extends SpringTestCase {
    private static final String TEST_NAME1 = "jUnit test -task 1";

    private static final String TEST_NAME2 = "jUnit test -task 2";

    private static final String TEST_DESC1 = "Task, missä tehdään vaikka mitä 1";

    private static final String TEST_DESC2 = "Task, missä tehdään vaikka mitä 2";

    private static final AFTime TEST_EST1 = new AFTime("4h");

    private static final AFTime TEST_EST2 = new AFTime("5h");

    private static final Priority TEST_PRI1 = Priority.CRITICAL;

    private static final Priority TEST_PRI2 = Priority.TRIVIAL;

    private static final State TEST_STAT1 = State.NOT_STARTED;

    private static final State TEST_STAT2 = State.STARTED;

    private static final int INVALID_TASKID = -1;

    private User user1;

    private User user2;

    // The field and setter to be used by Spring
    private TaskAction taskAction;

    private UserAction userAction;

    private ProductAction productAction;

    private BacklogItemAction backlogItemAction;

    private TaskDAO taskDAO;

    private ProductDAO productDAO;

    private BacklogItemDAO backlogItemDAO;

    private UserDAO userDAO;

    private IterationDAO iterationDAO;

    // private SecurityUtil securityUtil;

    public void setTaskAction(TaskAction taskAction) {
        this.taskAction = taskAction;
    }

    public void setUserAction(UserAction userAction) {
        this.userAction = userAction;
    }

    public void setProductAction(ProductAction productAction) {
        this.productAction = productAction;
    }

    public void setBacklogItemAction(BacklogItemAction backlogItemAction) {
        this.backlogItemAction = backlogItemAction;
    }

    public void setTaskDAO(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
        this.backlogItemDAO = backlogItemDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /*
     * public void setSecurityUtil(SecurityUtil securityUtil) {
     * this.securityUtil = securityUtil; }
     */

    /*
     * Checks, if there are any given error countered.
     */
    @SuppressWarnings(value = "unchecked")
    private boolean errorFound(String e) {
        Collection<String> errors = (Collection<String>) taskAction
                .getActionErrors();
        boolean found = false;
        System.out.println("checking for errors.");
        for (String s : errors) {
            System.out.println("error " + s);
            if (s.equals(e))
                found = true;
        }
        return found;
    }

    private void setNameAndDesc(String name, String desc) {
        Task t = taskAction.getTask();
        t.setName(name);
        t.setDescription(desc);
    }

    private void setLoggedUser(User user) {
        SecurityUtil.setLoggedUser(user);
    }

    private void setPriority(Priority priority) {
        taskAction.getTask().setPriority(priority);
    }

    private void setState(State state) {
        taskAction.getTask().setState(state);
    }

    private void setBacklogItem(BacklogItem bi) {
        taskAction.setBacklogItemId(bi.getId());
    }

    private void setContents(String name, String desc, User creator,
            User assignee, AFTime estimate, Priority priority,
            State state, BacklogItem backlogItem) {
        this.setNameAndDesc(name, desc);
        this.setLoggedUser(creator);
        this.setPriority(priority);
        this.setState(state);
        this.setBacklogItem(backlogItem);
    }

    private void checkContents(String entity, Task task, String name,
            String desc, User creator, User assignee, AFTime estimate,
            Priority priority, State state, BacklogItem backlogItem) {
        super.assertEquals("The name of the " + entity + " was wrong", name,
                task.getName());
        super.assertEquals("The description of the " + entity + " was wrong",
                desc, task.getDescription());
        super.assertEquals("The creator of the " + entity + " was wrong",
                creator, task.getCreator());
        super.assertEquals("The priority of the " + entity + " was wrong",
                priority, task.getPriority());
        super.assertEquals("The state of the " + entity + " was wrong",
                state, task.getState());
        super.assertEquals("The backlog item of the " + entity + " was wrong",
                backlogItem, task.getBacklogItem());

    }

    private User getTestUser(int n) {
        if (n == 1) {
            if (this.user1 == null)
                user1 = UserActionTest.GenerateAndStoreTestUser(
                        this.userAction, this.userDAO, 1);
            assertNotNull(user1);
            return user1;
        } else {
            if (this.user2 == null)
                user2 = UserActionTest.GenerateAndStoreTestUser(
                        this.userAction, this.userDAO, 2);
            assertNotNull(user2);
            return user2;
        }
    }

    private Backlog getTestBacklog() {
        this.productAction.create();
        Product p = this.productAction.getProduct();
        p.setAssignee(this.getTestUser(1));
        p.setDescription("FOOBAR");
        p.setName("Testituote 1");
        String result = this.productAction.store();
        assertSame("Product creation failed", Action.SUCCESS, result);
        Collection<Product> cp = this.productDAO.getAll();
        for (Product pp : cp) {
            return pp;
        }
        fail("Product not created as supposed");
        return null;
    }

    private BacklogItem getTestBacklogItem(Backlog b) {
        this.backlogItemAction.setBacklogId(b.getId());
        this.backlogItemAction.create();
        BacklogItem bi = this.backlogItemAction.getBacklogItem();
        bi.setAssignee(this.getTestUser(1));
        bi.setBacklog(b);
        bi.setDescription("FOOBAR");
        bi.setName("FOOBAR");
        bi.setPriority(TEST_PRI1);
        bi.setOriginalEstimate(TEST_EST1);
        bi.setPriority(TEST_PRI1);
        String result = this.backlogItemAction.store();
        assertSame("Backlog item creation failed", Action.SUCCESS, result);
        Collection<BacklogItem> cb = this.backlogItemDAO.getAll();
        for (BacklogItem bb : cb) {
            return bb;
        }
        fail("Backlog item not created as supposed");
        return null;
    }

    private Collection<Task> getAllTasks() {
        return this.taskDAO.getAll();
    }

    /*
     * Method for calling taskAction.create that is supposed to work (and is not
     * a target for testing) Actual testing for method create is done in
     * testCreate_XXX -methods
     */
    private void create() {
        String result = taskAction.create();
        assertEquals("create() was unsuccessful", Action.SUCCESS, result);
    }

    /*
     * Method for calling taskAction.store that is supposed to work (and is not
     * a target for testing) Actual testing for method store is done in
     * testStore_XXX -methods
     */
    private void store() {
        String result = taskAction.store();
        assertEquals("store() was unsuccessful", Action.SUCCESS, result);
    }

    private void edit() {
        String result = taskAction.edit();
        assertEquals("edit() was unsuccessful", Action.SUCCESS, result);
    }

    private void fetchForEditing(String name, String desc) {
        this.taskAction.setTask(null);
        taskAction.setTaskId(this.getTask(name, desc).getId());
        this.edit();
        super.assertNotNull("Edit() was unsuccesfull", this.taskAction
                .getTask());
    }

    /*
     * Get task based on name and description.
     */
    private Task getTask(String name, String desc) {
        for (Task t : getAllTasks()) {
            if (t.getName().equals(name) && t.getDescription().equals(desc))
                return t;
        }
        return null;
    }

    /**
     * Generates a test task
     * 
     * @param ta
     *                springed TaskAction object
     * @param td
     *                springed TaskDAO object
     * @param effortEstimate
     *                Effort estimate for the task
     * @param number
     *                chosen test user (1 or 2)
     * @return Task object, that is stored.
     */
    /*
     * public static Task GenerateAndStoreTestUser(TaskAction ta, TaskDAO td,
     * BacklogItem bi, AFTime effortEstimate, int number) { TaskActionTest tat =
     * new TaskActionTest(); tat.setTaskAction(ta); tat.setTaskDAO(td);
     * tat.create(); tat.setEstimate(effortEstimate); User assignee =
     * tat.getTestUser(1); User creator = tat.getTestUser(2); String name;
     * String desc; if(number == 1) { name = TEST_NAME1; desc = TEST_DESC1;
     * tat.setContents(name, desc, creator, assignee, effortEstimate, TEST_PRI1,
     * TEST_STAT1, bi); } else { name = TEST_NAME2; desc = TEST_DESC2;
     * tat.setContents(name, desc, creator, assignee, effortEstimate, TEST_PRI2,
     * TEST_STAT2, bi); } tat.store(); return tat.getTask(name, desc); }
     */

    /** * Actual test methods * */

    public void testCreate() {
        String result = taskAction.create();
        assertEquals("create() was unsuccessful", result, Action.SUCCESS);
        super.assertEquals("New user had an invalid id", 0, taskAction
                .getTaskId());
        super.assertNotNull("Created taskaction had null Task", taskAction
                .getTask());
    }

    public void testStore() {
        this.create();
        User assignee = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER1);
        User creator = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER2);
        BacklogItem bi = this.getTestBacklogItem(this.getTestBacklog());
        this.setContents(TEST_NAME1, TEST_DESC1, creator, assignee, TEST_EST1,
                TEST_PRI1, TEST_STAT1, bi);
        int n = this.getAllTasks().size();
        String result = taskAction.store();
        super.assertEquals("store() was unsuccessful", result, Action.SUCCESS);
        super
                .assertEquals(
                        "The total number of stored tasks didn't grow up with store().",
                        n + 1, getAllTasks().size());
        Task storedTask = this.getTask(TEST_NAME1, TEST_DESC1);
        super.assertNotNull("Stored task wasn't found", storedTask);
        this.checkContents("stored task", storedTask, TEST_NAME1, TEST_DESC1,
                creator, assignee, TEST_EST1, TEST_PRI1, TEST_STAT1, bi);
    }

    public void testStore_withEmptyName() {
        this.create();
        User assignee = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER1);
        User creator = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER2);
        BacklogItem bi = this.getTestBacklogItem(this.getTestBacklog());
        this.setContents("", TEST_DESC1, creator, assignee, TEST_EST1,
                TEST_PRI1, TEST_STAT1, bi);
        String result = taskAction.store();
        super.assertEquals("storing task with name missing was successful",
                Action.ERROR, result);
        assertTrue("task.missingName -error not found", errorFound(taskAction
                .getText("task.missingName")));
    }

    public void testStore_withoutCreate() {
        try {
            String result = taskAction.store();
            assertEquals("Store without create didn't result an error.",
                    Action.ERROR, result);
        } catch (NullPointerException e) {
        }
    }

    public void testEdit() {
        this.create();
        User assignee = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER1);
        User creator = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER2);
        BacklogItem bi = this.getTestBacklogItem(this.getTestBacklog());
        this.setContents(TEST_NAME1, TEST_DESC1, creator, assignee, TEST_EST1,
                TEST_PRI1, TEST_STAT1, bi);
        this.store();
        taskAction.setTask(null);
        Task temp = this.getTask(TEST_NAME1, TEST_DESC1);
        taskAction.setTaskId(temp.getId());
        String result = taskAction.edit();
        super.assertEquals("edit() was unsuccessful", result, Action.SUCCESS);
        Task fetchedTask = taskAction.getTask();
        this.checkContents("task fetched for editing", fetchedTask, TEST_NAME1,
                TEST_DESC1, creator, assignee, TEST_EST1, TEST_PRI1,
                TEST_STAT1, bi);
    }

    public void testEdit_withInvalidId() {
        userAction.setUserId(INVALID_TASKID);
        String result = userAction.edit();
        assertEquals("Invalid task id didn't result an error.", Action.ERROR,
                result);
        // TODO check the error?
    }

    /*
     * Change the details of previously stored task and update the task.
     */
    public void testStore_withUpdate() {
        this.create();
        User assignee = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER1);
        User creator = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER2);
        BacklogItem bi = this.getTestBacklogItem(this.getTestBacklog());
        this.setContents(TEST_NAME1, TEST_DESC1, creator, assignee, TEST_EST1,
                TEST_PRI1, TEST_STAT1, bi);
        this.store();

        taskAction.setTask(null);
        taskAction.setTaskId(this.getTask(TEST_NAME1, TEST_DESC1).getId());
        this.edit();

        this.setNameAndDesc(TEST_NAME2, TEST_DESC2);
        this.setPriority(TEST_PRI2);
        this.setState(TEST_STAT2);

        String result = taskAction.store();
        super.assertEquals("store() was unsuccessful", result, Action.SUCCESS);

        Task updatedTask = this.getTask(TEST_NAME2, TEST_DESC2);
        this.checkContents("updated task", updatedTask, TEST_NAME2, TEST_DESC2,
                creator, creator, TEST_EST2, TEST_PRI2, TEST_STAT2, bi);
    }

    public void testDelete() {
        this.create();
        User assignee = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER1);
        User creator = TestUtility.initUser(userAction, userDAO,
                TestUtility.TestUser.USER2);
        BacklogItem bi = this.getTestBacklogItem(this.getTestBacklog());
        this.setContents(TEST_NAME1, TEST_DESC1, creator, assignee, TEST_EST1,
                TEST_PRI1, TEST_STAT1, bi);
        this.store();

        int n = getAllTasks().size();
        taskAction.setTaskId(this.getTask(TEST_NAME1, TEST_DESC1).getId());
        String result = taskAction.delete();
        super.assertEquals("delete() was unsuccessful", result, Action.SUCCESS);
        super.assertEquals(
                "The number of tasks didn't decrease with delete().", n - 1,
                getAllTasks().size());

        Task test = this.getTask(TEST_NAME1, TEST_DESC1);
        super.assertNull("The deleted task wasn't properly deleted", test);
    }

    public void testDelete_withInvalidId() {
        taskAction.setTaskId(INVALID_TASKID);
        try {
            String result = taskAction.delete();
            assertEquals("Invalid task id didn't result an error.",
                    Action.ERROR, result);
            // TODO - check error.
            // fail("delete() with invalid id " + INVALID_TASKID + " was
            // accepted.");
        } catch (IllegalArgumentException iae) {
        }
    }

//    public void testBLIEffLeft() {
//        final long MINUTE = 60000;
//        final long HOUR = MINUTE * 60;
//        int iterationId;
//        String result;
//        long origEffort = HOUR + 10 * MINUTE;
//        long taskEffort = 20 * MINUTE;
//        long workDone = 10 * MINUTE;
//        int testTaskId;
//        BacklogItem[] backlogItemArray;
//
//        /* Test setup */
//        TestUtility.initUser(userAction, userDAO);
//        iterationId = TestUtility.createTestIteration(1, iterationDAO);
//        assertNotNull("Iteration creation failed", iterationDAO
//                .get(iterationId));
//        result = TestUtility.createTestItem(iterationDAO.get(iterationId),
//                backlogItemAction, origEffort);
//        assertFalse("Item creation failed: "
//                + backlogItemAction.getActionErrors(), result
//                .equals(Action.ERROR));
//        backlogItemArray = (BacklogItem[]) backlogItemDAO.getAll().toArray(
//                new BacklogItem[0]);

        /* Test original estimate */
//        assertEquals("Calculating BLI effort left failed", backlogItemDAO. 
//                .getBliEffortLeftSum(backlogItemArray[0]), new AFTime(origEffort));
//        assertNull("Calculation task effort left failed", backlogItemDAO
//                .getTaskSumEffortLeft(backlogItemArray[0]));

        /* Test creating a new task */
//        testTaskId = TestUtility.createTestTask(backlogItemArray[0],
//                taskAction, taskEffort);
//        assertEquals("Calculation task effort left failed", new AFTime(
//                taskEffort), backlogItemDAO
//                .getTaskSumEffortLeft(backlogItemArray[0]));
//        assertEquals("Calculating BLI effort left failed", new AFTime(
//                origEffort), backlogItemDAO
//                .getBLIEffortLeft(backlogItemArray[0]));

        /*
         * Test creating a new task which decreases placeholder effort left to
         * zero
         */
//        testTaskId = TestUtility.createTestTask(backlogItemArray[0],
//                taskAction, 100 * HOUR);
//        assertEquals("Placeholder effor estimate decreased bellow zero",
//                new AFTime(0), backlogItemArray[0].getPlaceHolder()
//                        .getEffortEstimate());
//    }

    public IterationDAO getIterationDAO() {
        return iterationDAO;
    }

    public void setIterationDAO(IterationDAO iterationDAO) {
        this.iterationDAO = iterationDAO;
    }
}
