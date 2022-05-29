import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { ACCESS_TOKEN } from '../constants'
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button'
import CardActions from '@material-ui/core/CardActions'
import CardContent from '@material-ui/core/CardContent'
import CardUi from '@material-ui/core/Card'
import IconButton from '@material-ui/core/IconButton'
import ListItem from '@material-ui/core/ListItem'
import TextField from '@material-ui/core/TextField'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'
import EditIcon from '@material-ui/icons/Edit'

function Workspace (props) {
  const [newWorkspaceName, setNewWorkspaceName] = useState(props.name)

  const workspaceNameChangeHandler = (event) => {
    setNewWorkspaceName(event.target.value)
  }

  const createWorkspaceSubmitHandler = (event) => {
    event.preventDefault()
    const createWorkspace = async () => {
      const headers = {
        'Content-Type': 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ name: newWorkspaceName })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      return await response.json()
    }
    createWorkspace()
      .then((workspace) => {
        props.handleCreateWorkspace(workspace.id, newWorkspaceName)
        setNewWorkspaceName('')
      })
      .catch((err) => {
        console.log('Error while creating the Workspace with name "' + newWorkspaceName + '": ' + err.message)
        props.handleCreateWorkspaceError(err.message)
        setNewWorkspaceName('')
      })
  }

  const updateWorkspaceSubmitHandler = (event) => {
    event.preventDefault()
    const updateWorkspace = async () => {
      if (props.name === newWorkspaceName) {
        return
      }
      const headers = {
        'Content-Type': 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.id, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({ name: newWorkspaceName })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      return await response.json()
    }
    updateWorkspace()
      .then((workspace) => {
        props.handleUpdateWorkspaceComplete(workspace.id, newWorkspaceName)
      })
      .catch((err) => {
        console.log('Error while updating the Workspace with id "' + props.id + '": ' + err.message)
        props.handleUpdateWorkspaceError(err.message, props.name)
        setNewWorkspaceName('')
      })
  }

  const updateWorkspaceCancelHandler = (event) => {
    event.preventDefault()
    setNewWorkspaceName(props.name)
    props.handleUpdateWorkspaceCancel(props.id)
  }

  const useStyles = makeStyles((theme) => ({
    input: {
      width: 260
    },
    workspace: {
      width: '100%',
      textAlign: 'left'
    },
    actions: {
      display: 'flex',
      justifyContent: 'flex-end'
    },
    expand: {
      marginLeft: 'auto',
      marginTop: 'auto'
    }
  }))
  const classes = useStyles()

  if (props.new) {
    return (
            <ListItem button selected={props.selected} >
                <form onSubmit={createWorkspaceSubmitHandler}>
                    <Box display="flex" flexWrap="wrap" p={0} justifyContent="flex-start" alignItems="center">
                        <Box p={1}>
                            <TextField id="new-workspace-name" label="New workspace" required variant="outlined" InputProps={{ className: classes.input }} value={newWorkspaceName} onChange={workspaceNameChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="primary" type="submit">Create</Button>
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="secondary" onClick={props.handleCreateWorkspaceCancel}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
    )
  } else if (props.change) {
    return (
          <ListItem button selected={props.selected} >
              <form onSubmit={updateWorkspaceSubmitHandler}>
                  <Box display="flex" flexWrap="wrap" p={0} justifyContent="flex-start" alignItems="center">
                      <Box p={1}>
                          <TextField id="new-workspace-name" label="New workspace" required variant="outlined" InputProps={{ className: classes.input }} value={newWorkspaceName} onChange={workspaceNameChangeHandler} />
                      </Box>
                      <Box p={1}>
                          <Button variant="contained" color="primary" type="submit">Update</Button>
                      </Box>
                      <Box p={1}>
                          <Button variant="contained" color="secondary" onClick={updateWorkspaceCancelHandler}>Cancel</Button>
                      </Box>
                  </Box>
              </form>
          </ListItem>
    )
  } else {
    return (
            <ListItem button selected={props.selected} >
                <CardUi className={classes.workspace}>
                    <CardContent component={Link} to={'/workspaces/' + props.id + '/cards'} style={{ textDecoration: 'none' }}>
                        <Box display="flex" flexWrap="wrap" p={0} m={0}>
                            <Box pl={2}>
                                <Typography variant="body1" color="textSecondary" component="p" gutterBottom>
                                    <b>{props.name}</b>
                                </Typography>
                            </Box>
                        </Box>
                    </CardContent>
                    <CardActions className={classes.actions}>
                        <IconButton aria-label="edit" onClick={() => props.handleUpdateWorkspace(props.id)}>
                            <EditIcon className={classes.expand}/>
                        </IconButton>
                    </CardActions>
                </CardUi>
            </ListItem>
    )
  }
}

export default Workspace
