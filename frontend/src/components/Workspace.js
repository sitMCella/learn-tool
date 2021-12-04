import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button'
import ListItem from '@material-ui/core/ListItem'
import ListItemText from '@material-ui/core/ListItemText'
import TextField from '@material-ui/core/TextField'
import { makeStyles } from '@material-ui/core/styles'

function Workspace (props) {
  const [newWorkspaceName, setNewWorkspaceName] = useState('')

  const workspaceNameChangeHandler = (event) => {
    setNewWorkspaceName(event.target.value)
  }

  const submitHandler = (event) => {
    event.preventDefault()
    const createWorkspace = async () => {
      const response = await fetch('/api/workspaces', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: newWorkspaceName })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      return await response.json()
    }
    createWorkspace()
      .then(() => {
        props.handleSubmit(newWorkspaceName)
        setNewWorkspaceName('')
      })
      .catch((err) => {
        console.log('Error while creating the Workspace ' + newWorkspaceName + ': ' + err.message)
        props.handleError(err.message)
        setNewWorkspaceName('')
      })
  }

  const useStyles = makeStyles((theme) => ({
    input: {
      width: 260
    }
  }))
  const classes = useStyles()

  if (props.new) {
    return (
            <ListItem button selected={props.selected} >
                <form onSubmit={submitHandler}>
                    <Box display="flex" flexWrap="wrap" p={0} justifyContent="flex-start" alignItems="center">
                        <Box p={1}>
                            <TextField id="new-workspace-name" label="New workspace" required variant="outlined" InputProps={{ className: classes.input }} value={newWorkspaceName} onChange={workspaceNameChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="primary" type="submit">Create</Button>
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="secondary" onClick={props.handleCancel}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
    )
  } else {
    return (
            <ListItem button selected={props.selected} component={Link} to={'/workspaces/' + props.name} >
                <ListItemText primary={props.name} />
            </ListItem>
    )
  }
}

export default Workspace
