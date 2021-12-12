import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
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
import Rating from '@material-ui/lab/Rating'
import Typography from '@material-ui/core/Typography'
import Toolbar from '@material-ui/core/Toolbar'
import { makeStyles } from '@material-ui/core/styles'
import DashboardIcon from '@material-ui/icons/Dashboard'
import FilterNoneIcon from '@material-ui/icons/FilterNone'
import StarIcon from '@material-ui/icons/Star'

function Study () {
  const params = useParams()
  const [cardId, setCardId] = useState('')
  const [cardQuestion, setCardQuestion] = useState('')
  const [cardResponse, setCardResponse] = useState('')
  const [flipButtonVisible, setFlipButtonVisible] = useState(true)
  const [responseVisibility, setResponseVisibility] = useState('none')
  const [evaluationButtonsVisible, setEvaluationButtonsVisible] = useState(false)
  const [noCardsLft, setNoCardsLeft] = useState(false)
  const [qualityValue, setQualityValue] = React.useState(3)

  const getCard = async (signal) => {
    const response = await fetch('/api/workspaces/' + params.name + '/learn', {
      method: 'GET',
      headers: {
        Accepted: 'application/json'
      },
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
    getCard(signal).catch((err) => {
      console.log('Error while retrieving a card from the Workspace ' + params.name + ': ' + err.message)
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
      const response = await fetch('/api/workspaces/' + params.name + '/learn/' + cardId, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Accepted: 'application/json'
        },
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
        setEvaluationButtonsVisible(false)
        setResponseVisibility('none')
        setFlipButtonVisible(true)
        getCard()
          .catch((err) => {
            console.log('Error while retrieving a card from the Workspace ' + params.name + ' status: ' + err.message)
            setNoCardsLeft(true)
            setCardId('')
            setCardQuestion('')
            setCardResponse('')
          })
      })
      .catch((err) => {
        console.log('Error while evaluating the Card with Id ' + cardId + ': ' + err.message)
        setCardId('')
        setCardQuestion('')
        setCardResponse('')
      })
  }

  const useStyles = makeStyles((theme) => ({
    appBar: {
      marginBottom: theme.spacing(2)
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
    },
    questionCard: {
      width: '95%',
      backgroundColor: '#F7CAC9'
    },
    responseCard: {
      paddingTop: 10,
      width: '95%'
    }
  }))
  const classes = useStyles()

  return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar variant="dense">
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
                    <CardUi className={classes.card}>
                        <CardContent className={classes.cardContent}>
                            <Box display="flex" flexWrap="wrap" p={0} m={0}>
                                <Box display="flex" flexWrap="wrap" p={1} className={classes.questionCard}>
                                    <Typography variant="body1" color="textSecondary" component="p" gutterBottom >
                                        {cardQuestion}
                                    </Typography>
                                </Box>
                            </Box>
                            <Box display={responseVisibility}>
                                <Box display="flex" flexWrap="wrap" p={0} m={1} className={classes.responseCard}>
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
        </Box>
  )
}

export default Study
