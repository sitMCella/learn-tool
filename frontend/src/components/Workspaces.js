import React, { useState, useEffect } from 'react';
import Workspace from "./Workspace";
import { makeStyles } from '@material-ui/core/styles';
import List from '@material-ui/core/List';
import { Button } from '@material-ui/core';
import MuiAlert from '@material-ui/lab/Alert';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';

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
            const response = await fetch('/api/workspaces', {
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
            .catch(() => {
                setWorkspaceError(true);
                setWorkspaceErrorMessage("Cannot retrieve the Workspaces, please refresh the page.")
            });
    }, []);
    const newWorkspaceHandler = () => {
        if(newWorkspaceStatus) {
            return;
        }
        const newWorkspaces = [{ name: "New Workspace Name", new: true }, ...list];
        setList(newWorkspaces);
        setNewWorkspaceStatus(true);
        setWorkspaceError(false);
    };
    const submitHandler = (workspaceName) => {
        const newWorkspaces = [{ name: workspaceName, new: false }, ...list.slice(1)];
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    }
    const createErrorHandler = (errCode) => {
        const newWorkspaces = list.slice(1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
        setWorkspaceError(true);
        if(errCode === "422") {
            setWorkspaceErrorMessage("Cannot create the Workspace. The Workspace name should not contain spaces.");
        } else if(errCode === "409") {
            setWorkspaceErrorMessage("Cannot create the Workspace. The Workspace already exists.");
        } else {
            setWorkspaceErrorMessage("Cannot create the Workspace.");
        }
    }
    const cancelButtonClickHandler = () => {
        const newWorkspaces = list.slice(1);
        setList(newWorkspaces);
        setNewWorkspaceStatus(false);
    };
    const useStyles = makeStyles((theme) => ({
        appBar: {
            marginBottom: theme.spacing(2),
        },
        menuButton: {
            marginRight: theme.spacing(2),
        },
        title: {
            marginRight: theme.spacing(10),
        },
    }));
    const classes = useStyles();
    return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <IconButton className={classes.menuButton} edge="start" color="inherit" aria-label="menu">
                        <MenuIcon />
                    </IconButton>
                    <Button color="inherit" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>New Workspace</Button>
                </Toolbar>
            </AppBar>
            <div>
                {workspaceError && (<Alert severity="error">{workspaceErrorMessage}</Alert>)}
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(workspace => <Workspace key={workspace.name} name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
                </List>
            </div>
        </div>
    );
}

export default Workspaces;
