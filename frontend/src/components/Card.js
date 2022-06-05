import React, { useState } from 'react'
import { ACCESS_TOKEN } from '../constants'
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button'
import CardUi from '@material-ui/core/Card'
import CardContent from '@material-ui/core/CardContent'
import CardActions from '@material-ui/core/CardActions'
import IconButton from '@material-ui/core/IconButton'
import ListItem from '@material-ui/core/ListItem'
import TextField from '@material-ui/core/TextField'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'
import DeleteIcon from '@material-ui/icons/Delete'
import EditIcon from '@material-ui/icons/Edit'

function Card (props) {
  const [newQuestion, setNewQuestion] = useState(props.question)
  const [newResponse, setNewResponse] = useState(props.response)

  const questionChangeHandler = (event) => {
    setNewQuestion(event.target.value)
  }
  const responseChangeHandler = (event) => {
    setNewResponse(event.target.value)
  }

  const createCardSubmitHandler = (event) => {
    event.preventDefault()
    const createCard = async () => {
      const headers = {
        'Content-Type': 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.workspaceId + '/cards', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ workspaceId: props.workspaceId, question: newQuestion, response: newResponse })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      return await response.json()
    }
    const createLearnCard = async (cardId) => {
      const headers = {
        'Content-Type': 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.workspaceId + '/learn/' + cardId, {
        method: 'POST',
        headers: headers
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
    }
    createCard()
      .then((card) => {
        props.handleCreateCard(card.id, card.question, card.response)
        setNewQuestion('')
        setNewResponse('')
        createLearnCard(card.id)
          .catch((err) => {
            throw err
          })
      })
      .catch((err) => {
        console.log('Error while creating the Card: ' + err.message)
        props.handleCreateCardError()
        setNewQuestion('')
        setNewResponse('')
      })
  }

  const updateCardSubmitHandler = (event) => {
    event.preventDefault()
    const updateCard = async () => {
      if (props.question === newQuestion && props.response === newResponse) {
        return
      }
      const headers = {
        'Content-Type': 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.workspaceId + '/cards/' + props.id, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({ question: newQuestion, response: newResponse })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      return await response.json()
    }
    updateCard()
      .then((card) => {
        props.handleUpdateCardComplete(card.id, card.question, card.response)
      })
      .catch((err) => {
        console.log('Error while updating the Card with id "' + props.id + '": ' + err.message)
        props.handleUpdateCardError(props.id, props.question, props.response)
        setNewQuestion('')
        setNewResponse('')
      })
  }

  const updateCardCancelHandler = (event) => {
    event.preventDefault()
    setNewQuestion(props.question)
    setNewResponse(props.response)
    props.handleUpdateCardCancel(props.id)
  }

  const deleteCardHandler = (event) => {
    event.preventDefault()
    const deleteLearnCard = async () => {
      const headers = {}
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.workspaceId + '/learn/' + props.id, {
        method: 'DELETE',
        headers: headers
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
    }
    const deleteCard = async () => {
      const headers = {}
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + props.workspaceId + '/cards/' + props.id, {
        method: 'DELETE',
        headers: headers
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
    }
    deleteLearnCard()
      .then(() => {
        deleteCard()
          .then(() => props.handleDeleteCardComplete(props.id))
          .catch((err) => {
            throw err
          })
      })
      .catch((err) => {
        console.log('Error while deleting the Card: ' + err.message)
        props.handleDeleteCardError(err.message)
      })
  }

  const useStyles = makeStyles(() => ({
    formContent: {
      width: '100%'
    },
    input: {
      width: 1100,
      '@media only screen and (max-width:768px)': {
        width: 260
      }
    },
    card: {
      width: '100%',
      textAlign: 'left'
    },
    cardTitle: {
      width: 80
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
                <form onSubmit={createCardSubmitHandler}>
                    <Box display="flex" flexWrap="wrap" p={0} justifyContent="flex-start" alignItems="center">
                        <Box p={1} className={classes.formContent}>
                            <TextField id="new-card-question" label="New question" required variant="outlined" InputProps={{ className: classes.input }} value={newQuestion} onChange={questionChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <TextField id="new-card-response" label="New response" required variant="outlined" InputProps={{ className: classes.input }} value={newResponse} onChange={responseChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="primary" type="submit">Create</Button>
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="secondary" onClick={props.handleCreateCardCancel}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
    )
  } else if (props.change) {
    return (
            <ListItem button selected={props.selected} >
                <form onSubmit={updateCardSubmitHandler}>
                    <Box display="flex" flexWrap="wrap" p={0} justifyContent="flex-start" alignItems="center">
                        <Box p={1} className={classes.formContent}>
                            <TextField id="new-card-question" label="New question" required variant="outlined" InputProps={{ className: classes.input }} value={newQuestion} onChange={questionChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <TextField id="new-card-response" label="New response" required variant="outlined" InputProps={{ className: classes.input }} value={newResponse} onChange={responseChangeHandler} />
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="primary" type="submit">Update</Button>
                        </Box>
                        <Box p={1}>
                            <Button variant="contained" color="secondary" onClick={updateCardCancelHandler}>Cancel</Button>
                        </Box>
                    </Box>
                </form>
            </ListItem>
    )
  } else {
    return (
            <ListItem selected={props.selected}>
                <CardUi className={classes.card}>
                    <CardContent>
                        <Box display="flex" flexWrap="wrap" p={0} m={0}>
                            <Box pr={2} className={classes.cardTitle}>
                                <Typography variant="body1" color="textSecondary" component="p" gutterBottom>
                                    <b>Question:</b>
                                </Typography>
                            </Box>
                            <Box p={0}>
                                <Typography variant="body1" color="textSecondary" component="p" gutterBottom style={{ color: 'var(--card-question-text-color)' }}>
                                    {props.question}
                                </Typography>
                            </Box>
                        </Box>
                        <Box display="flex" flexWrap="wrap" p={0} m={0}>
                            <Box pr={2} className={classes.cardTitle}>
                                <Typography variant="body1" color="textSecondary" component="p" gutterBottom>
                                    <b>Response:</b>
                                </Typography>
                            </Box>
                            <Box p={0}>
                                <Typography variant="body1" color="textSecondary" component="p" gutterBottom style={{ color: 'var(--card-response-text-color)' }}>
                                    {props.response}
                                </Typography>
                            </Box>
                        </Box>
                    </CardContent>
                    <CardActions className={classes.actions}>
                        <IconButton aria-label="edit" onClick={() => props.handleUpdateCard(props.id)}>
                            <EditIcon className={classes.expand}/>
                        </IconButton>
                        <IconButton aria-label="delete" onClick={deleteCardHandler}>
                            <DeleteIcon/>
                        </IconButton>
                    </CardActions>
                </CardUi>
            </ListItem>
    )
  }
}

export default Card
