import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ACCESS_TOKEN } from '../constants'
import ProfileMenu from './ProfileMenu'
import StudySettings from './StudySettings'
import AppBar from '@material-ui/core/AppBar'
import Box from '@material-ui/core/Box'
import Button from '@material-ui/core/Button'
import ButtonBase from '@material-ui/core/ButtonBase'
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
import SettingsIcon from '@material-ui/icons/Settings'
import SkipNext from '@material-ui/icons/SkipNext'
import BeatLoader from 'react-spinners/BeatLoader'

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
  const [settingsVisible, setSettingsVisible] = useState(false)
  const [pageLoading, setPageLoading] = useState(true)

  const getCard = async (signal) => {
    const headers = {
      Accepted: 'application/json'
    }
    if (localStorage.getItem(ACCESS_TOKEN)) {
      headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
    }
    const response = await fetch('/api/workspaces/' + params.id + '/learn', {
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
      .then(() => {
        setStudyError(false)
        setPageLoading(false)
      })
      .catch((err) => {
        console.log('Error while retrieving a card from the Workspace ' + params.id + ': ' + err.message)
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
      const response = await fetch('/api/workspaces/' + params.id + '/learn/' + cardId, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify({ quality: 0 })
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      setCardId('')
    }
    evaluateCard()
      .then(() => {
        getCard()
          .catch((err) => {
            console.log('Error while retrieving the next card from the Workspace with id ' + params.id + ' status: ' + err.message)
            setStudyError(true)
            setStudyErrorMessage('Cannot retrieve the next Card, please refresh the page.')
            setNoCardsLeft(true)
            setCardId('')
            setCardQuestion('')
            setCardResponse('')
          })
          .finally(() => {
            setStudyError(false)
            setEvaluationButtonsVisible(false)
            setResponseVisibility('none')
            setFlipButtonVisible(true)
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
    setQualityValue(3)
  }

  const handleNextCard = () => {
    getCard()
      .then(() => setStudyError(false))
      .catch((err) => {
        console.log('Error while retrieving a card from the Workspace ' + params.id + ': ' + err.message)
        setStudyError(true)
        setStudyErrorMessage('Cannot retrieve the next Card, please refresh the page.')
        setNoCardsLeft(true)
        setCardId('')
        setCardQuestion('')
        setCardResponse('')
      })
  }

  const handleSettingsOpen = () => {
    setSettingsVisible(true)
  }

  const handleSettingsClose = () => {
    setSettingsVisible(false)
  }

  const useStyles = makeStyles((theme) => ({
    appBar: {
      '@media only screen and (max-width:768px)': {
        width: '98%'
      },
      marginBottom: theme.spacing(2),
      marginLeft: 20,
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
    drawerFooter: {
      position: 'fixed',
      bottom: '0',
      marginBottom: theme.spacing(2)
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
    errors: {
      marginBottom: theme.spacing(2)
    },
    title: {
      flex: 0,
      display: 'flex',
      position: 'absolute',
      alignItems: 'center',
      padding: theme.spacing(0, 1)
    },
    learnCard: {
      marginTop: 20
    },
    card: {
      minHeight: '40vh',
      width: '100%',
      textAlign: 'left'
    },
    cardContent: {
      textAlign: 'left',
      padding: theme.spacing(0, 0),
      width: '100%'
    },
    appbarBottom: {
      alignItems: 'center',
      width: '100%'
    },
    questionCard: {
      minWidth: '90%',
      maxWidth: '100%'
    },
    responseCard: {
      paddingTop: 10,
      minWidth: '90%',
      maxWidth: '100%'
    },
    events: {
      display: 'flex',
      justifyContent: 'flex-end',
      paddingRight: 5
    },
    eventIcon: {
      paddingRight: 5
    },
    close: {
      marginTop: theme.spacing(1)
    },
    flipButton: {
      minWidth: '40vh',
      height: '100%'
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
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces/' + params.id + '/cards'} className={classes.drawerListItem}>
                        <ListItemIcon><FilterNoneIcon /></ListItemIcon>
                    </ListItem>
                </List>
                <Box className={classes.drawerFooter}>
                  <Divider className={classes.divider} />
                  <List className={classes.drawerList} >
                    <ListItem button key="Settings" onClick={handleSettingsOpen} className={classes.drawerListItem}>
                      <ListItemIcon><SettingsIcon /></ListItemIcon>
                    </ListItem>
                  </List>
                </Box>
            </Drawer>
            { !noCardsLft
              ? (
                  <Box className={classes.content}>
                  {
                    settingsVisible
                      ? (
                        <StudySettings handleClose={handleSettingsClose} handleSettingsUpdate={props.onSettingsUpdate} {...props}/>
                        )
                      : (
                        <div>
                          {studyError && (<div className={classes.errors}><Alert severity="error">{studyErrorMessage}</Alert></div>)}
                          <div className={classes.title}>
                            <Typography variant="h5" color="textSecondary" component="p" gutterBottom>
                              Learn
                            </Typography>
                          </div>
                          <Box className={classes.events}>
                            <Box className={classes.eventIcon}>
                              <Fab size="small" color="primary" aria-label="add" disabled={pageLoading || evaluationButtonsVisible}>
                                <SkipNext onClick={handleNextCard} />
                              </Fab>
                            </Box>
                          </Box>
                          {
                            pageLoading
                              ? (
                                <BeatLoader color="#2196f3" />
                                )
                              : (
                                <List component="nav" aria-label="cards" className={classes.learnCard}>
                                  <CardUi className={classes.card}>
                                      <CardContent className={classes.cardContent}>
                                          <Box display="flex" flexWrap="wrap" p={0} m={0} style={{ backgroundColor: 'var(--study-card-question-background-color)' }}>
                                              <Box display="flex" flexWrap="wrap" pl={1} pt={1} pr={1} pb={1} m={0} className={classes.questionCard}>
                                                  <Typography variant="body1" color="textSecondary" component="p" gutterBottom style={{ color: 'var(--study-card-question-text-color)' }}>
                                                      {cardQuestion}
                                                  </Typography>
                                              </Box>
                                          </Box>
                                          <Box display={responseVisibility} style={{ backgroundColor: 'var(--study-card-response-background-color)' }}>
                                              <Box display="flex" flexWrap="wrap" pl={1} pt={1} pr={1} pb={1} m={0} className={classes.responseCard}>
                                                  <Box p={0}>
                                                      <Typography variant="body1" color="textSecondary" component="p" gutterBottom style={{ color: 'var(--study-card-response-text-color)' }}>
                                                          {cardResponse}
                                                      </Typography>
                                                  </Box>
                                              </Box>
                                          </Box>
                                      </CardContent>
                                  </CardUi>
                                  <AppBar position="static" className={classes.appbarBottom}>
                                      <Toolbar>
                                          {flipButtonVisible && (<ButtonBase className={classes.flipButton}><Button color="inherit" size="large" style={{ width: '100%', height: '100%' }} onClick={flipCardHandler}>Flip</Button></ButtonBase>)}
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
                                )
                          }
                        </div>
                        )
                  }
                  </Box>
                )
              : (
                <Box className={classes.content}>
                  {
                    settingsVisible
                      ? (
                          <StudySettings handleClose={handleSettingsClose} handleSettingsUpdate={props.onSettingsUpdate} {...props}/>
                        )
                      : (
                          <div>
                            <div className={classes.title}>Learn</div>
                            <Box className={classes.events}>
                              <Box className={classes.eventIcon}>
                                <Fab size="small" color="primary" aria-label="add">
                                  <SkipNext/>
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
                                <div className={classes.close}>
                                  <Button variant="contained" color="primary" component={Link}
                                          to={'/workspaces/' + params.id + '/cards'}>Close</Button>
                                </div>
                              </Box>
                            </List>
                          </div>
                        )
                  }
                </Box>
                )}
        </Box>
  )
}

export default Study
