import React, { useEffect, useState } from 'react'
import CardContent from '@material-ui/core/CardContent'
import Typography from '@material-ui/core/Typography'
import CardUi from '@material-ui/core/Card/Card'
import { Link, useParams } from 'react-router-dom'
import { Button } from '@material-ui/core'
import Box from '@material-ui/core/Box'
import { makeStyles } from '@material-ui/core/styles'
import Toolbar from '@material-ui/core/Toolbar'
import AppBar from '@material-ui/core/AppBar'
import Divider from '@material-ui/core/Divider'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import DashboardIcon from '@material-ui/icons/Dashboard'
import Drawer from '@material-ui/core/Drawer'
import FilterNoneIcon from '@material-ui/icons/FilterNone'

function Study () {
  const params = useParams()
  const [cardId, setCardId] = useState('')
  const [cardQuestion, setCardQuestion] = useState('')
  const [cardResponse, setCardResponse] = useState('')
  const [flipButtonVisible, setFlipButtonVisible] = useState(true)
  const [responseVisibility, setResponseVisibility] = useState('none')
  const [evaluationButtonsVisible, setEvaluationButtonsVisible] = useState(false)
  const [noCardsLft, setNoCardsLeft] = useState(false)
  const getCard = async () => {
    const response = await fetch('/api/workspaces/' + params.name + '/learn', {
      method: 'GET',
      headers: {
        Accepted: 'application/json'
      }
    })
    if (!response.ok) {
      throw new Error(response.status)
    }
    const card = await response.json()
    setCardId(card.id)
    setCardQuestion(card.question)
    setCardResponse(card.response)
  }
  useEffect(() => {
    getCard().catch((err) => {
      console.log('Error while retrieving a card from the Workspace ' + params.name + ' status: ' + err.message)
      setNoCardsLeft(true)
      setCardId('')
      setCardQuestion('')
      setCardResponse('')
    })
  }, [])
  const flipCardHandler = () => {
    setFlipButtonVisible(false)
    setResponseVisibility('block')
    setEvaluationButtonsVisible(true)
  }
  const evaluateCardHandler = () => {
    const evaluateCard = async () => {
      const response = await fetch('/api/workspaces/' + params.name + '/learn/' + cardId, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Accepted: 'application/json'
        },
        body: JSON.stringify({ quality: 0 })
      })
      if (!response.ok) {
        throw new Error('Error while evaluating the Card with Id ' + cardId)
      }
      setCardId('')
      setCardQuestion('')
      setCardResponse('')
    }
    evaluateCard().then(() => {
      setEvaluationButtonsVisible(false)
      setResponseVisibility('none')
      setFlipButtonVisible(true)
      getCard().catch((err) => {
        console.log('Error while retrieving a card from the Workspace ' + params.name + ' status: ' + err.message)
        setNoCardsLeft(true)
        setCardId('')
        setCardQuestion('')
        setCardResponse('')
      })
    }).catch((err) => {
      console.log(err)
      setCardId('')
      setCardQuestion('')
      setCardResponse('')
    })
  }
  const qualityValues = [0, 1, 2, 3, 4, 5]
  const useStyles = makeStyles((theme) => ({
    menuButton: {
      marginRight: theme.spacing(2),
      '@media only screen and (max-width:768px)': {
        display: 'none'
      }
    },
    appBar: {
      '@media only screen and (max-width:14000px)': {
        marginLeft: theme.spacing(5)
      },
      marginBottom: theme.spacing(2),
      zIndex: theme.zIndex.drawer + 1,
      transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      })
    },
    toolbar: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
      padding: theme.spacing(0, 1),
      ...theme.mixins.toolbar
    },
    divider: {
      '@media only screen and (max-width:768px)': {
        display: 'none'
      }
    },
    content: {
      width: '100%',
      '@media only screen and (max-width:14000px)': {
        marginLeft: theme.spacing(5)
      }
    },
    title: {
      marginRight: theme.spacing(10)
    },
    card: {
      minHeight: '40vh',
      width: '100%',
      textAlign: 'left'
    },
    cardContent: {
      textAlign: 'left',
      width: '100%'
    },
    appbarBottom: {
      alignItems: 'center',
      width: '100%'
    }
  }))
  const classes = useStyles()
  return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                </Toolbar>
            </AppBar>
            <Drawer variant="permanent" anchor="left">
                <div className={classes.toolbar}>
                </div>
                <Divider className={classes.divider} />
                <List>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces'}>
                        <ListItemIcon><DashboardIcon /></ListItemIcon>
                    </ListItem>
                </List>
                <List>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces/' + params.name + '/cards'}>
                        <ListItemIcon><FilterNoneIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            { !noCardsLft
              ? (
                <div className={classes.content}>
                    <CardUi className={classes.card}>
                        <CardContent className={classes.cardContent}>
                            <Box display="flex" flexWrap="wrap" p={0} m={1}>
                                <Box pr={2}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        <b>Question:</b>
                                    </Typography>
                                </Box>
                                <Box p={0}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        {cardQuestion}
                                    </Typography>
                                </Box>
                            </Box>
                            <Box display={responseVisibility}>
                                <Box display="flex" flexWrap="wrap" p={0} m={1}>
                                    <Box pr={2}>
                                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                            <b>Response:</b>
                                        </Typography>
                                    </Box>
                                    <Box p={0}>
                                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                            {cardResponse}
                                        </Typography>
                                    </Box>
                                </Box>
                            </Box>
                        </CardContent>
                    </CardUi>
                    <AppBar position="static" className={classes.appbarBottom}>
                        <Toolbar>
                            {flipButtonVisible && (<Button color="inherit" style={{ maxWidth: '50vh', minWidth: '50vh' }} onClick={flipCardHandler}>Flip</Button>)}
                            {evaluationButtonsVisible && (
                                <Box component="span" display={responseVisibility}>
                                    {qualityValues.map((value) => {
                                      return <Button key={value} color="inherit" onClick={() => evaluateCardHandler({ value })}>{value}</Button>
                                    })}
                                </Box>
                            )}
                        </Toolbar>
                    </AppBar>
                </div>
                )
              : (
                <div className={classes.content}>
                    <CardUi>
                        <CardContent>
                            <Typography variant="body2" color="textSecondary" component="p">
                                No cards left
                            </Typography>
                        </CardContent>
                    </CardUi>
                    <Box component="span" m={3}>
                        <Button variant="contained" color="primary" component={Link} to={'/workspaces/' + params.name + '/cards'}>Close</Button>
                    </Box>
                </div>
                )}
        </div>
  )
}

export default Study
