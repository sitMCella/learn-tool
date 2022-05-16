import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ACCESS_TOKEN } from '../constants'
import ProfileMenu from './ProfileMenu'
import AppBar from '@material-ui/core/AppBar'
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button'
import CardUi from '@material-ui/core/Card/Card'
import CardContent from '@material-ui/core/CardContent'
import Divider from '@material-ui/core/Divider'
import Drawer from '@material-ui/core/Drawer'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import MuiAlert from '@material-ui/lab/Alert'
import Rating from '@material-ui/lab/Rating'
import Typography from '@material-ui/core/Typography'
import Toolbar from '@material-ui/core/Toolbar'
import { makeStyles } from '@material-ui/core/styles'
import DashboardIcon from '@material-ui/icons/Dashboard'
import FilterNoneIcon from '@material-ui/icons/FilterNone'
import StarIcon from '@material-ui/icons/Star'
import Fab from '@material-ui/core/Fab'
import SkipNextIcon from '@material-ui/icons/SkipNext'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function Study (props) {
  const params = useParams()
  const [cardId, setCardId] = useState('')
  const [cardQuestion, setCardQuestion] = useState('')
  const [cardResponse, setCardResponse] = useState('')
  const [flipButtonVisible, setFlipButtonVisible] = useState(true)
  const [responseVisibility, setResponseVisibility] = useState('none')
  const [evaluationButtonsVisible, setEvaluationButtonsVisible] = useState(false)
  const [noCardsLft, setNoCardsLeft] = useState(false)
  const [qualityValue, setQualityValue] = React.useState(3)
  const [studyError, setStudyError] = useState(false)
  const [studyErrorMessage, setStudyErrorMessage] = useState('')

  const getCard = async (signal) => {
    const headers = {
      Accepted: 'application/json'
    }
    if (localStorage.getItem(ACCESS_TOKEN)) {
      headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
    }
    const response = await fetch('/api/workspaces/' + params.name + '/learn', {
      method: 'GET',
      headers: headers,
      signal
    })
    if (!response.ok) {
      throw new Error(JSON.stringify(response))
    }
    const card = await response.json()
    setCardId(card.id)
    setCardQuestion(card.question)
    setCardResponse(card.response)
  }

  useEffect(() => {
    const controller = new AbortController()
    const signal = controller.signal
    getCard(signal)
      .then(() => setStudyError(false))
      .catch((err) => {
        console.log('Error while retrieving a card from the Workspace ' + params.name + ': ' + err.message)
        setStudyError(true)
        setStudyErrorMessage('Cannot retrieve the next Card, please refresh the page.')
        setNoCardsLeft(true)
        setCardId('')
        setCardQuestion('')
        setCardResponse('')
      })
    return () => controller.abort()
  }, [])

  const flipCardHandler = () => {
    setFlipButtonVisible(false)
    setResponseVisibility('block')
    setEvaluationButtonsVisible(true)
  }

  const evaluateCardHandler = () => {
    const evaluateCard = async () => {
      const headers = {
        'Content-Type': 'application/json',
        Accepted: 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + params.name + '/learn/' + cardId, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({ quality: 0 })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      setCardId('')
      setCardQuestion('')
      setCardResponse('')
    }
    evaluateCard()
      .then(() => {
        setStudyError(false)
        setEvaluationButtonsVisible(false)
        setResponseVisibility('none')
        setFlipButtonVisible(true)
        getCard()
          .catch((err) => {
            console.log('Error while evaluating the card from the Workspace ' + params.name + ' status: ' + err.message)
            setStudyError(true)
            setStudyErrorMessage('Cannot evaluate the Card, please refresh the page.')
            setNoCardsLeft(true)
            setCardId('')
            setCardQuestion('')
            setCardResponse('')
          })
      })
      .catch((err) => {
        console.log('Error while evaluating the Card with Id ' + cardId + ': ' + err.message)
        setStudyError(true)
        setStudyErrorMessage('Cannot evaluate the Card, please refresh the page.')
        setCardId('')
        setCardQuestion('')
        setCardResponse('')
      })
  }

  const useStyles = makeStyles((theme) => ({
    appBar: {
      '@media only screen and (max-width:768px)': {
        marginLeft: 20
      },
      marginBottom: theme.spacing(2),
      marginLeft: 30,
      marginRight: 0
    },
    drawerList: {
      '@media only screen and (max-width:768px)': {
        width: 35,
        paddingLeft: 0
      },
      overflowX: 'hidden',
      width: 60
    },
    drawerListItem: {
      '@media only screen and (max-width:768px)': {
        paddingLeft: 5
      }
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
      '@media only screen and (max-width:14000px)': {
        marginLeft: theme.spacing(5)
      },
      marginRight: 0
    },
    title: {
      flex: 0,
      display: 'flex',
      position: 'absolute',
      alignItems: 'center',
      fontSize: 'x-large',
      padding: theme.spacing(0, 1)
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
    },
    questionCard: {
      '@media only screen and (max-width:768px)': {
        width: '90%'
      },
      width: '95%',
      backgroundColor: '#89CFF0'
    },
    responseCard: {
      '@media only screen and (max-width:768px)': {
        width: '90%'
      },
      paddingTop: 10,
      width: '95%'
    },
    events: {
      display: 'flex',
      justifyContent: 'flex-end',
      paddingRight: 5
    },
    eventIcon: {
      paddingRight: 5
    }
  }))
  const classes = useStyles()

  return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="relative" className={classes.appBar}>
                <Toolbar variant="dense">
                  <ProfileMenu {...props} />
                </Toolbar>
            </AppBar>
            <Drawer variant="permanent" anchor="left">
                <div className={classes.toolbar}>
                </div>
                <Divider className={classes.divider} />
                <List className={classes.drawerList}>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces'} className={classes.drawerListItem}>
                        <ListItemIcon><DashboardIcon /></ListItemIcon>
                    </ListItem>
                </List>
                <List className={classes.drawerList}>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces/' + params.name + '/cards'} className={classes.drawerListItem}>
                        <ListItemIcon><FilterNoneIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            { !noCardsLft
              ? (
                <div className={classes.content}>
                    {studyError && (<Alert severity="error">{studyErrorMessage}</Alert>)}
                    <div className={classes.title}>Learn</div>
                    <Box className={classes.events}>
                      <Box className={classes.eventIcon}>
                        <Fab size="small" color="primary" aria-label="add">
                          <SkipNextIcon onClick={flipCardHandler} />
                        </Fab>
                      </Box>
                    </Box>
                  <List component="nav" aria-label="cards">
                    <CardUi className={classes.card}>
                        <CardContent className={classes.cardContent}>
                            <Box display="flex" flexWrap="wrap" p={0} m={0}>
                                <Box display="flex" flexWrap="wrap" pl={1} pt={1} pb={1} m={0} className={classes.questionCard}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        {cardQuestion}
                                    </Typography>
                                </Box>
                            </Box>
                            <Box display={responseVisibility}>
                                <Box display="flex" flexWrap="wrap" pl={1} pt={1} pb={1} m={0} className={classes.responseCard}>
                                    <Box p={0}>
                                        <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                            {cardResponse}
                                        </Typography>
                                    </Box>
                                </Box>
                            </Box>
                        </CardContent>
                    </CardUi>
                    <AppBar position="static" className={classes.appbarBottom} fullWidth>
                        <Toolbar>
                            {flipButtonVisible && (<Button color="inherit" style={{ width: '100%' }} onClick={flipCardHandler}>Flip</Button>)}
                            {evaluationButtonsVisible && (
                                <Box component="span" display={responseVisibility} style={{ width: '100%' }}>
                                  <Rating display={responseVisibility}
                                      name="hover-feedback"
                                      value={qualityValue}
                                      precision={1}
                                      onChange={(event, newQualityValue) => {
                                        setQualityValue(newQualityValue)
                                        evaluateCardHandler({ newQualityValue })
                                      }}
                                      emptyIcon={<StarIcon style={{ opacity: 0.55 }} fontSize="inherit" />}
                                  />
                                </Box>
                            )}
                        </Toolbar>
                    </AppBar>
                  </List>
                </div>
                )
              : (
                <div className={classes.content}>
                    <div className={classes.title}>Learn</div>
                    <Box className={classes.events}>
                      <Box className={classes.eventIcon}>
                        <Fab size="small" color="primary" aria-label="add">
                          <SkipNextIcon />
                        </Fab>
                      </Box>
                    </Box>
                    <List component="nav" aria-label="cards">
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
                    </List>
                </div>
                )}
        </Box>
  )
}

export default Study
