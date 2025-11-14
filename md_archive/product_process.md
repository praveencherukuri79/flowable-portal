there requirement is,

there is a 3 stage process.



stage 1 => maker claim => edit products list => call a task listner delgate and store data to DB, close task => checker claim => approve/reject , complete task

stage 1 => maker claim => edit products list=> call a task listner delgate and store data to DB, close task => checker claim => approve/reject , complete task

stage 1 => maker claim => edit products list=> call a task listner delgate and store data to DB, close task => checker claim => approve/reject , complete task

************

create a BPMN with DI info for above.

***********

in above , treat each process with a sheetId, so that the list of edited products will be tied to that specific sheetId.


prodct will have rate and API and effective date.

Approver can approve each product in a sheet OR approve all, but we need to tack invidual approval.

*************

keep process API's away from business logic

during user task, send all data to API in custom field, task listner will call dedicated spring bean and handle the logic.

so, same API's can ne used for multiple processes.

BPMN user task will be calling different beans based on user tasks.

****************

use formKey of usertask for navigating to a specic page in UI

*******************

for admin , add more options like 
start a process, end a process
assign task to a user, reassign, manage a process variables etc.

