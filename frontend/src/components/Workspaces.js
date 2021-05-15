import React, { useState, useEffect } from 'react';
import Workspace from "./Workspace";
import List from '@material-ui/core/List';
import { Button } from '@material-ui/core';
import MuiAlert from '@material-ui/lab/Alert';

function Alert(props) {
    return <MuiAlert elevation={6} variant="filled" {...props} />;
}

function Workspaces() {
    const [list, setList] = useState([]);
    const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false);
    const [workspaceError, setWorkspaceError] = useState(false);
    const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('');
    useEffect(() => {
        const getWorkspaces = async () => {
            const response = await fetch('http://localhost:8080/workspaces', {
                method: 'GET',
                headers: {
                    'Accepted': 'application/json'
                }
            });
            const responseData = await response.json();
            const loadedWorkspaces = [];
            for (const key in responseData) {
                loadedWorkspaces.push({
                    name: responseData[key].name,
                    new: false
                })
            }
            setList(loadedWorkspaces);
        }
        getWorkspaces()
            .then(() => setWorkspaceError(false))
            .catch((err) => setWorkspaceErrorMessage("Cannot retrieve the Workspaces, please refresh the page."));
    }, []);
    const newWorkspaceHandler = () => {
        if(newWorkspaceStatus) {
            return;
        }
        const newWorkspaces = list.concat({ name: "New Workspace Name", new: true });
        setList(newWorkspaces);
        setNewWorkspaceStatus(true);
        setWorkspaceError(false);
    };
    const submitHandler = (workspaceName) => {
        const newWorkspaces = list.slice(0,-1).concat({ name: workspaceName, new: false });
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    }
    const createErrorHandler = (errCode) => {
        const newWorkspaces = list.slice(0,-1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
        setWorkspaceError(true);
        console.log(errCode);
        if(errCode === "422") {
            setWorkspaceErrorMessage("Cannot create the Workspace. The Workspace name should not contain spaces.");
        } else {
            setWorkspaceErrorMessage("Cannot create the Workspace.");
        }
    }
    const cancelButtonClickHandler = () => {
        const newWorkspaces = list.slice(0,-1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    };
    return (
        <div>
            <Button variant="contained" color="primary" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>New Workspace</Button>
            {workspaceError && (<Alert severity="error">{workspaceErrorMessage}</Alert>)}
            <List component="nav" aria-label="main mailbox folders">
                {list.map(workspace => <Workspace key={workspace.name} name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
            </List>
        </div>
    );
}

export default Workspaces;
