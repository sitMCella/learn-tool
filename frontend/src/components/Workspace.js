import React, { useState } from 'react';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import TextField from '@material-ui/core/TextField';
import { Button } from "@material-ui/core";
import Box from '@material-ui/core/Box';

function Workspace(props) {
    const [newWorkspaceName, setNewWorkspaceName] = useState('');
    const workspaceNameChangeHandler = (event) => {
        setNewWorkspaceName(event.target.value)
    };
    const submitHandler = (event) => {
        event.preventDefault();
        console.log(newWorkspaceName);
        const createWorkspace = async () => {
            const response = await fetch('http://localhost:8080/workspaces', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({name: newWorkspaceName})
            });
            const workspace = await response.json();
            props.handleSubmit(workspace.name);
            setNewWorkspaceName('');
        };
        createWorkspace();
    };
    if (props.new) {
        return (
            <ListItem
                button
                selected={props.selected}
            >
                <form onSubmit={submitHandler}>
                    <Box display="flex" justifyContent="flex-start" alignItems="center">
                        <Box>
                            <TextField id="new-workspace-name" label="New workspace" variant="outlined" value={newWorkspaceName} onChange={workspaceNameChangeHandler} />
                        </Box>
                        <Box ml="1rem">
                            <Button variant="contained" color="primary" type="submit">Create</Button>
                        </Box>
                        <Box ml="1rem">
                            <Button variant="contained" color="secondary" onClick={props.handleCancel}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
        );
    } else {
        return (
            <ListItem
                button
                selected={props.selected}
            >
                <ListItemText primary={props.name} />
            </ListItem>
        );
    }
}

export default Workspace;
