import React, { useState, useEffect } from 'react';
import Workspace from "./Workspace";
import List from '@material-ui/core/List';
import { Button } from '@material-ui/core';

function Workspaces() {
    const [list, setList] = useState([]);
    const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false);
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
        getWorkspaces();
    }, []);
    const newWorkspaceHandler = () => {
        if(newWorkspaceStatus) {
            return;
        }
        const newWorkspaces = list.concat({ name: "New Workspace Name", new: true });
        setList(newWorkspaces);
        setNewWorkspaceStatus(true);
    };
    const submitHandler = (workspaceName) => {
        const newWorkspaces = list.slice(0,-1).concat({ name: workspaceName, new: false });
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    }
    const cancelButtonClickHandler = () => {
        const newWorkspaces = list.slice(0,-1)
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    };
    return (
        <div>
            <Button variant="contained" color="primary" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>New Workspace</Button>
            <List component="nav" aria-label="main mailbox folders">
                {list.map(workspace => <Workspace name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
            </List>
        </div>
    );
}

export default Workspaces;
