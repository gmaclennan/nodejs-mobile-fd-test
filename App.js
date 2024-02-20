import { StatusBar } from 'expo-status-bar'
import React from 'react'
import { StyleSheet, Text, View, Pressable } from 'react-native'
import { getFile } from './modules/saf-file-reader'
import nodejs from 'nodejs-mobile-react-native'

nodejs.start('main.js')

export default function App () {
  const [state, setState] = React.useState('loading')
  const [nodeStatus, setNodeStatus] = React.useState('loading')
  const [content, setContent] = React.useState('loading')
  const handlePress = () => {
    getFile().then(res => {
      nodejs.channel.post('fd', { fd: `/dev/fd/${res.fd}` /* or fd: res.fd */ })
      setState(JSON.stringify(res))
    })
  }
  React.useEffect(() => {
    const { remove } = nodejs.channel.addListener('message', msg => {
      setNodeStatus('loaded')
    })
    return () => remove()
  }, [])
  React.useEffect(() => {
    const { remove } = nodejs.channel.addListener('read', msg => {
      setContent(msg)
    })
    return () => remove()
  }, [])
  return (
    <View style={styles.container}>
      <Text>File: {state}</Text>
      <Text>Node: {nodeStatus}</Text>
      <Text>Content: {content.slice(0, 50)}</Text>
      <View style={{ height: 20 }}></View>
      <Pressable onPress={handlePress}>
        <Text>Open File</Text>
      </Pressable>
      <StatusBar style='auto' />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center'
  }
})
